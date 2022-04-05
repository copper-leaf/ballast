package com.copperleaf.ballast.test

public interface BallastScenarioInputSequenceScope<Inputs : Any, Events : Any, State : Any> {

    /**
     * Send an input to the ViewModel for processing, and wait for it to be completely finished processing before
     * advancing to the next input. Because this method will suspend until the input is completely finished, it will
     * never be cancelled by a new Input being sent too quickly.
     */
    public suspend fun sendAndAwait(input: Inputs): State

    /**
     * Send an input to the ViewModel for processing, but do not wait for it to finish processing. It will wait only
     * until the Input has been delivered to the InputHandler and started processing, but will return at that moment,
     * most likely getting cancelled by the next Input to be sent. Useful for testing filtering behavior.
     */
    public suspend fun send(input: Inputs)

    /**
     * An alias for [BallastScenarioInputSequenceScope.sendAndAwait].
     */
    public suspend operator fun Inputs.unaryPlus() {
        sendAndAwait(this)
    }

    /**
     * An alias for [BallastScenarioInputSequenceScope.send].
     */
    public suspend operator fun Inputs.unaryMinus() {
        send(this)
    }
}
