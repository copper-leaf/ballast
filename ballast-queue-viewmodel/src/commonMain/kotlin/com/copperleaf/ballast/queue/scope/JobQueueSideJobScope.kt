package com.copperleaf.ballast.queue.scope

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.internal.actors.InputActor
import com.copperleaf.ballast.internal.actors.InterceptorActor
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration

internal class JobQueueSideJobScope<Inputs : Any, Events : Any, State : Any>(
    sideJobCoroutineScope: CoroutineScope,

    override val logger: BallastLogger,

    private val inputActor: InputActor<Inputs, Events, State>,
    private val interceptorActor: InterceptorActor<Inputs, Events, State>,

    override val key: String,
    override val restartState: SideJobScope.RestartState,
    private val shutDownGracePeriod: Duration
) : SideJobScope<Inputs, Events, State>, CoroutineScope by sideJobCoroutineScope {

    override suspend fun postInput(input: Inputs) {
        inputActor.enqueueQueued(Queued.HandleInput(null, input), await = false)
    }

    override suspend fun postEvent(event: Events) {
        error("Events cannot be posted from SideJobs in JobQueueInputStrategy")
    }

    override suspend fun requestGracefulShutdown() {
        inputActor.enqueueQueued(Queued.ShutDownGracefully(null, shutDownGracePeriod), await = false)
    }

    override suspend fun <I : BallastInterceptor<*, *, *>> getInterceptor(key: BallastInterceptor.Key<I>): I {
        return interceptorActor.getInterceptor(key)
    }
}
