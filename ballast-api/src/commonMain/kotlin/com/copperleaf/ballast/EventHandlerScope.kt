package com.copperleaf.ballast

/**
 * The actions availble when handling an Event.
 */
@BallastDsl
public interface EventHandlerScope<Inputs : Any, Events : Any, State : Any> {

    /**
     * A reference to the [BallastLogger] set in the host ViewModel's [BallastViewModelConfiguration]
     * ([BallastViewModelConfiguration.logger]).
     */
    public val logger: BallastLogger

    /**
     * As the response to handling an Event, post another Input back to the VM.
     */
    public suspend fun postInput(input: Inputs)

    /**
     * Get an Interceptor registered to this ViewModel by its key.
     */
    public suspend fun <I: BallastInterceptor<*, *, *>> getInterceptor(key: BallastInterceptor.Key<I>): I
}
