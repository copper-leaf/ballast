package com.copperleaf.ballast

import kotlinx.coroutines.channels.Channel

/**
 * An interface for handling Events emitted by the ViewModel. Events are one-time side-effects that _must_ be handled
 * exactly once, though not necessarily immediately. Events are only sent as the result of processing an Input, where
 * they will be sent back to the ViewModel, buffered into a [Channel], and then dispatched asynchronously to the
 * registered EventHandler. Events will be run on the Dispatcher provided to the [BallastViewModelConfiguration]
 * ([BallastViewModelConfiguration.eventsDispatcher]).
 *
 * In most cases, a ViewModel should have exactly 1 EventHandler attached to it. If no EventHandlers are attached, then
 * Events may get queued up and eventually cause the [Channel] to suspend, blocking the main queue. More than one
 * EventHandler may be attached at once to distribute that work amongst the in parallel, but because they are both
 * reading from the same [Channel], each Event will only be sent to one of the Handlers. If you have a need for multiple
 * things to respond to all Events, use a [BallastInterceptor] instead.
 *
 * EventHandlers are typically platform-specific. While the InputHandler should be done in common code, it may need to
 * request some action that cannot be easily handled in common code, such as navigation. So instead of performing the
 * navigation in the InputHandler, which has no knowledge of platform-specific APIs, it delegates that action to the
 * EventHandler, which does.
 *
 * Because EventHandlers are platform-specific, the manner in which they are attached to the ViewModel is also
 * platform-specific. Refer to the documentation for your chosen ViewModel base class to see how to attach the
 * EventHandler on that platform.
 */
public interface EventHandler<Inputs : Any, Events : Any, State : Any> {

    /**
     * Asynchronously handle an Event. Prefer using a sealed class to describe all Events, and a `when` expression to
     * ensure you're handling all Events. This method will be running on the Dispatcher provided to the
     * [BallastViewModelConfiguration] ([BallastViewModelConfiguration.eventsDispatcher]).
     */
    public suspend fun EventHandlerScope<Inputs, Events, State>.handleEvent(
        event: Events
    )
}
