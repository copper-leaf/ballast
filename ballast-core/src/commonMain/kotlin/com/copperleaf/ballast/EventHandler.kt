package com.copperleaf.ballast

/**
 * An interface for handling Events emitted by the ViewModel. Events are typically used to initiate side-jobs, but
 * they may also need to post additional inputs back to the ViewModel.
 *
 * For example, an Event may dispatch a request to display a notification bubble. The notification UI is agnostic to
 * what is calling it, but we can attach a button handler to that request that, when clicked, emits an Input back to
 * the original VM that sent the event. One could write an implementation that does this outside the VM scope itself,
 * but then we have to deal with manually managing multiple references to the VM itself. In contrast, the
 * [EventHandlerScope] can be passed to the notification handler as an implicit reference to the originating VM,
 * simplifying the management and helpding prevent leaky or unnecessary abstractions around such code. This will post an
 * Input back to the ViewModel, and ultimately back to the [InputHandler] that dispatched this Event.
 *
 * The handling of an Event is typically considered a "side-job", running in parallel to the ViewModel. It cannot
 * reference the ViewModel State itself, and so any State in the Event is the State that was current at the time the
 * Event was dispatched, but may not necessarily be the exact same State by the time the Event is actually handled.
 */
public interface EventHandler<Inputs : Any, Events : Any, State : Any> {

    /**
     * Asynchronously handle an Event. Prefer using a sealed class to describe all Events, and a `when` expression to
     * ensure you're handling all Events.
     */
    public suspend fun EventHandlerScope<Inputs, Events, State>.handleEvent(
        event: Events
    )
}
