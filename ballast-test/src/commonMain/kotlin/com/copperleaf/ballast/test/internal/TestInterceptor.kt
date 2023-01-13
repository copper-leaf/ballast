package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.awaitViewModelStart
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.test.TestResults
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * An internal class used to keep the test framework
 */
@ExperimentalTime
internal class TestInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val testCoroutineScope: CoroutineScope,
    private val onTestComplete: CompletableDeferred<ScenarioResult<Inputs, Events, State>>,
    private val scenario: BallastScenarioScopeImpl<Inputs, Events, State>,
    private val testSequenceTimeout: Duration,
) : BallastInterceptor<Inputs, Events, State> {

    private val loggerInterceptor = LoggingInterceptor<Inputs, Events, State>()

    private var timedOut: Boolean = true
    private val acceptedInputs = mutableListOf<Inputs>()
    private val rejectedInputs = mutableListOf<Inputs>()
    private val droppedInputs = mutableListOf<Inputs>()
    private val successfulInputs = mutableListOf<Inputs>()
    private val cancelledInputs = mutableListOf<Inputs>()
    private val inputHandlerErrors = mutableListOf<Pair<Inputs, Throwable>>()

    private val events = mutableListOf<Events>()
    private val successfulEvents = mutableListOf<Events>()
    private val eventHandlerErrors = mutableListOf<Pair<Events, Throwable>>()

    private val states = mutableListOf<State>()

    private val sideJobs = mutableListOf<Pair<String, SideJobScope.RestartState>>()
    private val completedSideJobs = mutableListOf<String>()
    private val sideJobErrors = mutableListOf<Pair<String, Throwable>>()

    private val unhandledErrors = mutableListOf<Throwable>()

    override fun BallastInterceptorScope<Inputs, Events, State>.start(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        collectTestInfo(notifications)
        runTest(notifications)
    }

    private fun BallastInterceptorScope<Inputs, Events, State>.collectTestInfo(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ): Job {
        return launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .onEach { notification ->
//                    loggerInterceptor.onNotify(logger, notification)
                    when (notification) {
                        is BallastNotification.InputAccepted -> {
                            acceptedInputs += notification.input
                        }

                        is BallastNotification.InputRejected -> {
                            rejectedInputs += notification.input
                        }

                        is BallastNotification.InputDropped -> {
                            droppedInputs += notification.input
                        }

                        is BallastNotification.InputHandledSuccessfully -> {
                            successfulInputs += notification.input
                        }

                        is BallastNotification.InputCancelled -> {
                            cancelledInputs += notification.input
                        }

                        is BallastNotification.InputHandlerError -> {
                            inputHandlerErrors += notification.input to notification.throwable
                        }

                        is BallastNotification.EventEmitted -> {
                            events += notification.event
                        }

                        is BallastNotification.EventHandledSuccessfully -> {
                            successfulEvents += notification.event
                        }

                        is BallastNotification.EventHandlerError -> {
                            eventHandlerErrors += notification.event to notification.throwable
                        }

                        is BallastNotification.StateChanged -> {
                            states += notification.state
                        }

                        is BallastNotification.SideJobStarted -> {
                            sideJobs += notification.key to notification.restartState
                        }

                        is BallastNotification.SideJobCompleted -> {
                            completedSideJobs += notification.key
                        }

                        is BallastNotification.SideJobError -> {
                            sideJobErrors += notification.key to notification.throwable
                        }

                        is BallastNotification.UnhandledError -> {
                            unhandledErrors += notification.throwable
                        }

                        else -> {}
                    }
                }
                .launchIn(this)
        }
    }

    private fun BallastInterceptorScope<Inputs, Events, State>.runTest(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ): Job {
        return launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.awaitViewModelStart()
            val mark = TimeSource.Monotonic.markNow()

            // the ViewModel's CoroutineScope will be cancelled after `CloseGracefully` is completed. This will ensure
            // that this block gets executed, and the test framework will then be guaranteed to receive the result
            coroutineContext.job.invokeOnCompletion {
                completeTest(mark.elapsedNow())
                if(timedOut) {
                    testCoroutineScope.cancel()
                }
            }

            withTimeout(testSequenceTimeout) {
                scenario.onInputSequenceBlock(BallastScenarioInputSequenceScopeImpl(this@runTest))
                delay(250.milliseconds)
                val deferred = CompletableDeferred<Unit>()
                sendToQueue(Queued.ShutDownGracefully(deferred, gracePeriod = testSequenceTimeout))
                deferred.await()
            }
            timedOut = false
        }
    }

    private fun completeTest(scenarioTestTime: Duration) {
        val testResult = try {
            val testResults = TestResults(
                timedOut = timedOut,
                testDuration = scenarioTestTime,

                acceptedInputs = acceptedInputs.toList(),
                rejectedInputs = rejectedInputs.toList(),
                droppedInputs = droppedInputs.toList(),
                successfulInputs = successfulInputs.toList(),
                cancelledInputs = cancelledInputs.toList(),
                inputHandlerErrors = inputHandlerErrors.toList(),

                events = events.toList(),
                eventHandlerErrors = eventHandlerErrors.toList(),

                states = states.toList(),

                sideJobs = sideJobs.toList(),
                sideJobErrors = sideJobErrors.toList(),

                unhandledErrors = unhandledErrors.toList(),
            )
            scenario.verifyBlock(testResults)
            ScenarioResult.Passed(scenario, scenarioTestTime)
        } catch (e: Throwable) {
            ScenarioResult.Failed(scenario, scenarioTestTime, e)
        }
        onTestComplete.complete(testResult)
    }
}
