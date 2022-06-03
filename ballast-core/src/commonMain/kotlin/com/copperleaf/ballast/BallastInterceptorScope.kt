package com.copperleaf.ballast

import kotlinx.coroutines.CoroutineScope

/**
 * The DSL for processing Notifications from a [BallastInterceptor].
 *
 * This scope is itself a [CoroutineScope], so coroutines can be launched directly in it. Those coroutines are tied to
 * the parent ViewModel's [CoroutineScope], and will be cancelled when the ViewModel is cleared.
 */
public interface BallastInterceptorScope<Inputs : Any, Events : Any, State : Any> : CoroutineScope {

    /**
     * A reference to the [BallastLogger] set in the host ViewModel's [BallastViewModelConfiguration]
     * ([BallastViewModelConfiguration.logger]).
     */
    public val logger: BallastLogger

    /**
     * A reference to the host ViewModel's name, which may be used for differentiating between different ViewModels.
     * This value comes from the host ViewModel's [BallastViewModelConfiguration] ([BallastViewModelConfiguration.name]).
     */
    public val hostViewModelName: String

    /**
     * Send a [Queued] object back to the ViewModel to be processed. These items are queued just the same as if they
     * were sent to the ViewModel by something else through [BallastViewModel.send],
     * [BallastViewModel.sendAndAwaitCompletion], or [BallastViewModel.trySend].
     *
     * In addition to sending Inputs, the State can be restored to an arbitrary object. States will be restored
     * following the same rules as Inputs, being queued and processed at the appropriate time as determined by the
     * [InputStrategy] set in the host ViewModel's [BallastViewModelConfiguration]
     * ([BallastViewModelConfiguration.inputStrategy]).
     */
    public suspend fun sendToQueue(queued: Queued<Inputs, Events, State>)

}
