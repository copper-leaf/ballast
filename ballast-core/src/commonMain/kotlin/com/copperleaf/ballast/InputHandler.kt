package com.copperleaf.ballast

/**
 * A handler for processing Inputs that were accepted by the ViewModel. Inputs are processed serially, and only 1 Input
 * will be processed at a time. If it takes time to process any single Input, the handler will suspend and subsequent
 * Inputs will be queued up to be processed when the handler becomes free.
 *
 * Inputs can be handled in one of three ways (or any combination thereof), but each input _must_ be handled or else it
 * will be considered an error. The ways an input can be handled are:
 *
 * - Update State: given the current State, derive the next State. The State should be immutable, and updates to the
 *   state should be done by copying the State and modifying the relevant properties (leaving the rest unchanged).
 *   State updates are atomic with respect to the ViewModel's [StateFlow] that holds it.
 * - Post Event: Dispatch an Event to the ViewModel's [EventHandler], which may optionally be packed with values from
 *   the current State. Note that by the time the Event is actually handled, it may not be the same State that is
 *   currently in the ViewModel.
 * - Side-job: While most side-jobs should be dispatched as Events, there may be some side-job code that
 *   doesn't (or can't) be run as an Event (such as updating the DB). This code typically is not reflected in the State,
 *   but handling it as an Event would just be redundant, so it can neither be handled by updating the state or
 *   dispatching an event. To ensure that the rest of the Inputs are handled correctly, such code should be wrapped in
 *   a side-job so it is not marked as a handler error. Side-job handlers may dispatch both new Events (for a
 *   proper side-job) and new Inputs (for subsequent processing) but they cannot be posted with the current state
 *   like a normal handler, as that would break the guarantee of ordering that the ViewModel handlers normally provide.
 */
public interface InputHandler<Inputs : Any, Events : Any, State : Any> {

    /**
     * Asynchronously handle an Input. Prefer using a sealed class to describe all Inputs, and a `when` expression to
     * ensure you're handling all inputs.
     */
    public suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
        input: Inputs
    )
}
