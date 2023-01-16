package com.copperleaf.ballast.test

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.SideJobScope
import kotlin.time.Duration

public data class TestResults<Inputs : Any, Events : Any, State : Any>(

    /**
     * True if the test timed out before the input sequence and graceful shutdown was able to complete normally.
     */
    public val timedOut: Boolean,

    /**
     * How long the test took to run to completion.
     */
    public val testDuration: Duration,

    /**
     * All values intercepted by [BallastInterceptor.onInputAccepted] during the test.
     */
    public val acceptedInputs: List<Inputs>,

    /**
     * All values intercepted by [BallastInterceptor.onInputRejected] during the test.
     */
    public val rejectedInputs: List<Inputs>,

    /**
     * All values intercepted by [BallastInterceptor.onInputDropped] during the test.
     */
    public val droppedInputs: List<Inputs>,

    /**
     * All values intercepted by [BallastInterceptor.onInputHandledSuccessfully] during the test.
     */
    public val successfulInputs: List<Inputs>,

    /**
     * All values intercepted by [BallastInterceptor.onInputCancelled] during the test.
     */
    public val cancelledInputs: List<Inputs>,

    /**
     * All values intercepted by [BallastInterceptor.onInputHandlerError] during the test.
     */
    public val inputHandlerErrors: List<Pair<Inputs, Throwable>>,

    /**
     * All values intercepted by [BallastInterceptor.onEventEmitted] during the test.
     */
    public val events: List<Events>,

    /**
     * All values intercepted by [BallastInterceptor.onEventHandlerError] during the test.
     */
    public val eventHandlerErrors: List<Pair<Events, Throwable>>,

    /**
     * All values intercepted by [BallastInterceptor.onStateEmitted] during the test.
     */
    public val states: List<State>,

    /**
     * All values intercepted by [BallastInterceptor.onSideJobStarted] during the test.
     */
    public val sideJobs: List<Pair<String, SideJobScope.RestartState>>,

    /**
     * All values intercepted by [BallastInterceptor.onSideJobError] during the test.
     */
    public val sideJobErrors: List<Pair<String, Throwable>>,

    /**
     * All values intercepted by [BallastInterceptor.onUnhandledError] during the test.
     */
    public val unhandledErrors: List<Throwable>,
) {

    /**
     * Convenience accessor for the final State after the test has completed.
     */
    public val latestState: State = states.last()
}
