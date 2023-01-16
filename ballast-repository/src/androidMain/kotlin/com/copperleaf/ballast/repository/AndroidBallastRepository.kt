package com.copperleaf.ballast.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.repository.bus.EventBus
import com.copperleaf.ballast.repository.bus.EventBusEventHandler
import kotlinx.coroutines.launch

/**
 * An implementation of [BallastRepository] built on top of the standard Android ViewModel, so that the repository can
 * be tied more properly into the Android Application lifecycle, or be scoped to NavGraphs if needed.
 */
public open class AndroidBallastRepository<Inputs : Any, State : Any>
private constructor(
    private val impl: BallastViewModelImpl<Inputs, Any, State>,
    private val eventBus: EventBus,
) : ViewModel(), BallastViewModel<Inputs, Any, State> by impl {

    public constructor(
        eventBus: EventBus,
        config: BallastViewModelConfiguration<Inputs, Any, State>,
    ) : this(
        impl = BallastViewModelImpl("AndroidBallastRepository", config),
        eventBus = eventBus,
    )

    init {
        impl.start(viewModelScope)
        viewModelScope.launch {
            impl.attachEventHandler(EventBusEventHandler(eventBus))
        }
    }
}
