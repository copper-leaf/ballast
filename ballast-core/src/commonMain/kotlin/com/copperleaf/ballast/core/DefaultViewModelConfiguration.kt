package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.FilteredInputHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public class DefaultViewModelConfiguration<Inputs : Any, Events : Any, State : Any>(
    override val initialState: State,
    override val inputHandler: InputHandler<Inputs, Events, State>,
    override val filter: InputFilter<Inputs, Events, State>?,
    override val interceptors: List<BallastInterceptor<Inputs, Events, State>>,
    override val inputStrategy: InputStrategy,
    override val inputsDispatcher: CoroutineDispatcher,
    override val eventsDispatcher: CoroutineDispatcher,
    override val sideEffectsDispatcher: CoroutineDispatcher,
    override val interceptorDispatcher: CoroutineDispatcher,
    override val name: String,
) : BallastViewModelConfiguration<Inputs, Events, State> {

    public class Builder(
        public var name: String? = null
    ) {
        public var initialState: Any? = null
        public var inputHandler: InputHandler<*, *, *>? = null
        public var filter: InputFilter<*, *, *>? = null
        public val interceptors: MutableList<BallastInterceptor<*, *, *>> = mutableListOf()
        public var inputStrategy: InputStrategy = LifoInputStrategy()
        public var inputsDispatcher: CoroutineDispatcher = Dispatchers.Default
        public var eventsDispatcher: CoroutineDispatcher = Dispatchers.Default
        public var sideEffectsDispatcher: CoroutineDispatcher = Dispatchers.Default
        public var interceptorDispatcher: CoroutineDispatcher = Dispatchers.Default

        // Builder API
        // -------------------------------------------------------------------------------------------------------------

        public fun <Inputs : Any, Events : Any, State : Any> forViewModel(
            initialState: State,
            inputHandler: InputHandler<Inputs, Events, State>,
            filter: InputFilter<Inputs, Events, State>? = null,
            name: String? = this.name,
        ): Builder = apply {
            this.initialState = initialState
            this.inputHandler = inputHandler
            this.filter = filter
            this.name = name
        }

        public fun <Inputs : Any, Events : Any, State : Any> forViewModel(
            initialState: State,
            inputHandler: FilteredInputHandler<Inputs, Events, State>,
            name: String? = this.name,
        ): Builder = apply {
            this.initialState = initialState
            this.inputHandler = inputHandler
            this.filter = inputHandler
            this.name = name
        }

        public fun dispatchers(
            inputsDispatcher: CoroutineDispatcher,
            eventsDispatcher: CoroutineDispatcher = inputsDispatcher,
            sideEffectsDispatcher: CoroutineDispatcher = inputsDispatcher,
            interceptorDispatcher: CoroutineDispatcher = inputsDispatcher,
        ): Builder = apply {
            this.inputsDispatcher = inputsDispatcher
            this.eventsDispatcher = eventsDispatcher
            this.sideEffectsDispatcher = sideEffectsDispatcher
            this.interceptorDispatcher = interceptorDispatcher
        }

        public operator fun <Inputs : Any, Events : Any, State : Any> plusAssign(interceptor: BallastInterceptor<Inputs, Events, State>) {
            this.interceptors += interceptor
        }

        public operator fun <Inputs : Any, Events : Any, State : Any> plusAssign(interceptors: Iterable<BallastInterceptor<Inputs, Events, State>>) {
            this.interceptors += interceptors
        }

        // For internal use
        // -------------------------------------------------------------------------------------------------------------
        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> Any?.requireTyped(name: String): T {
            if (this == null) error("$name required")
            return this as T
        }

        @Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
        private fun <T : Any> Any?.requireTypedIfPresent(name: String): T? {
            if (this == null) return null
            return this as T
        }

        @Suppress("UNCHECKED_CAST")
        private fun <Inputs : Any, Events : Any, State : Any> List<BallastInterceptor<*, *, *>>.mapAsTyped(
        ): List<BallastInterceptor<Inputs, Events, State>> {
            return this.map { it as BallastInterceptor<Inputs, Events, State> }
        }

        public fun <Inputs : Any, Events : Any, State : Any> build(
        ): BallastViewModelConfiguration<Inputs, Events, State> {
            return DefaultViewModelConfiguration<Inputs, Events, State>(
                initialState = initialState.requireTyped("initialState"),
                inputHandler = inputHandler.requireTyped("inputHandler"),
                filter = filter.requireTypedIfPresent("filter"),
                interceptors = interceptors.mapAsTyped(),
                inputStrategy = inputStrategy,
                inputsDispatcher = inputsDispatcher,
                eventsDispatcher = eventsDispatcher,
                sideEffectsDispatcher = sideEffectsDispatcher,
                interceptorDispatcher = interceptorDispatcher,
                name = name ?: "$inputHandler-vm",
            )
        }
    }
}
