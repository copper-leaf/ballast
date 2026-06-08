package com.copperleaf.ballast.queue

import com.copperleaf.ballast.BallastScopeFactory
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.core.DefaultGuardian
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.queue.executor.DefaultQueueExecutor
import com.copperleaf.ballast.queue.scope.JobQueueInputStrategyScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/**
 * A normal InputStrategy directly reads from the input queue to handle Inputs. A JobQueue instead uses the channel
 * simply as a buffer to read them and place into a persistent queue. Separately, a job polls the queue to pull items
 * off the queue and process them.
 *
 * Inputs must be serializable, since they are stored persistently. Additionally, Inputs can be given a priority so that
 * the order in which they are processed is not necessarily the order in which they were received.
 */
@OptIn(InternalCoroutinesApi::class)
public class JobQueueInputStrategy<Inputs : Any, Events : Any, State : Any, JobMetadata : Any>(
    private val queueName: String,
    private val driver: QueueDriver<JobMetadata>,
    private val adapter: QueueDriver.Adapter<JobMetadata, Inputs, Events, State>,
    private val captureErrorStacktrace: Boolean = false,
) : InputStrategy<Inputs, Events, State> {

    private lateinit var queueExecutor: QueueExecutor<JobMetadata, Inputs, Events, State>
    private lateinit var inputStrategyScope: JobQueueInputStrategyScope<Inputs, Events, State>

    override fun InputStrategyScope<Inputs, Events, State>.start() {
        require(this is JobQueueInputStrategyScope<Inputs, Events, State>)
        requireNotNull(impl.decoder)
        inputStrategyScope = this

        queueExecutor = DefaultQueueExecutor(
            driver = driver,
            adapter = adapter,
            serializers = BallastQueueSerializers(impl.encoder, impl.decoder!!),
            captureErrorStacktrace = captureErrorStacktrace,
            timeSource = TimeSource.Monotonic,
        )

        queueExecutor
            .runQueue(queueName) { payload ->
                val queueExecutorScope = this
                processJobInViewModel(queueExecutorScope, payload)
            }
            .launchIn(this)
    }

    override suspend fun enqueue(queued: Queued<Inputs, Events, State>) {
        when (queued) {
            is Queued.HandleInput -> {
                queueExecutor.insertJob(queueName, queued.input, inputStrategyScope.impl.initialState)
            }

            is Queued.RestoreState -> {
                inputStrategyScope.acceptQueued(queued, DefaultGuardian()) { }
            }

            is Queued.ShutDownGracefully -> {
                // Launch the wait as a separate coroutine so enqueue() returns immediately. This keeps
                // the strategy loop unblocked, allowing in-flight jobs to continue enqueuing sub-jobs
                // (via insertJob) while we wait for the queue to drain. Only once the driver signals
                // that all active jobs have finished do we forward the shutdown to Ballast.
                inputStrategyScope.launch {
                    driver.awaitShutdown()
                    inputStrategyScope.acceptQueued(queued, DefaultGuardian()) { }
                }
            }
        }
    }

    override fun tryEnqueue(queued: Queued<Inputs, Events, State>): ChannelResult<Unit> {
        return if (inputStrategyScope.isActive) {
            inputStrategyScope.launch {
                enqueue(queued)
            }
            ChannelResult.success(Unit)
        } else {
            ChannelResult.failure()
        }
    }

    override fun close() {
    }

    override suspend fun flush() {
    }

    override fun getScopeFactory(impl: BallastViewModelImpl<Inputs, Events, State>): BallastScopeFactory<Inputs, Events, State> {
        return JobQueueScopeFactory(impl)
    }

// Process job in ViewModel
// ---------------------------------------------------------------------------------------------------------------------

    private suspend fun processJobInViewModel(
        queueExecutorScope: QueueExecutorScope<State>,
        payload: Inputs,
    ): Events? {
        val queuedInput = Queued.HandleInput<Inputs, Events, State>(null, payload)
        val guardian = JobQueueGuardian<Events, State>(queueExecutorScope)
        var error: Throwable? = null
        inputStrategyScope.acceptQueued(
            queued = queuedInput,
            guardian = guardian,
            onFailed = { error = it },
            onCancelled = { },
        )

        if (error != null) {
            // the queue executor expects an exception to be thrown as a signal for failure
            throw error
        } else {
            // if no exception was throw, the queue executor will acknowledge the job as successful
            return guardian.resultEvent
        }
    }
}
