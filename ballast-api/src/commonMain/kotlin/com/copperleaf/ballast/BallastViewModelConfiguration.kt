package com.copperleaf.ballast

import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.NoOpLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * This class collects all the configurable properties of a [BallastViewModel].
 */
public interface BallastViewModelConfiguration<Inputs : Any, Events : Any, State : Any> {
    public val initialState: State
    public val inputHandler: InputHandler<Inputs, Events, State>
    public val filter: InputFilter<Inputs, Events, State>?
    public val interceptors: List<BallastInterceptor<Inputs, Events, State>>
    public val inputStrategy: InputStrategy<Inputs, Events, State>
    public val inputsDispatcher: CoroutineDispatcher
    public val eventsDispatcher: CoroutineDispatcher
    public val sideJobsDispatcher: CoroutineDispatcher
    public val interceptorDispatcher: CoroutineDispatcher
    public val name: String
    public val logger: BallastLogger

    public data class Builder(
        public var name: String? = null,
        public var initialState: Any? = null,
        public var inputHandler: InputHandler<*, *, *>? = null,
        public var filter: InputFilter<*, *, *>? = null,
        public val interceptors: MutableList<BallastInterceptor<*, *, *>> = mutableListOf(),
        public var inputStrategy: InputStrategy<*, *, *> = LifoInputStrategy(),
        public var inputsDispatcher: CoroutineDispatcher = Dispatchers.Default,
        public var eventsDispatcher: CoroutineDispatcher = Dispatchers.Default,
        public var sideJobsDispatcher: CoroutineDispatcher = Dispatchers.Default,
        public var interceptorDispatcher: CoroutineDispatcher = Dispatchers.Default,
        public var logger: (String) -> BallastLogger = { NoOpLogger() },
    )
}
