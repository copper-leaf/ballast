package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * A generic ViewModel for Kotlin targets that don't have their own platform-specific ViewModel, or for anywhere you
 * want to manually control the lifecycle of the ViewModel. BasicViewModel's lifecycle is controlled by a coroutineScope
 * provided to it upon creation. When the scope gets cancelled, the ViewModel gets closed and can not be used again.
 */
public open class BasicViewModel<Inputs : Any, Events : Any, State : Any> private constructor(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    private val eventHandler: EventHandler<Inputs, Events, State>,
    coroutineScope: CoroutineScope
) : BallastViewModel<Inputs, Events, State> by impl {

    final override val type: String = "BasicViewModel"

    public constructor(
        config: BallastViewModelConfiguration<Inputs, Events, State>,
        eventHandler: EventHandler<Inputs, Events, State>,
        coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(
        BallastViewModelImpl(config),
        eventHandler,
        coroutineScope,
    )

    init {
        impl.start(coroutineScope) { this@BasicViewModel }
        impl.viewModelScope.launch {
            impl.attachEventHandler(eventHandler)
        }
    }
}
