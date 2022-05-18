package com.copperleaf.ballast.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public open class AndroidViewModel<Inputs : Any, Events : Any, State : Any>
private constructor(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
) : ViewModel(), BallastViewModel<Inputs, Events, State> by impl {

    final override val type: String = "AndroidViewModel"

    public constructor(
        config: BallastViewModelConfiguration<Inputs, Events, State>,
    ) : this(BallastViewModelImpl(config))

    init {
        impl.start(viewModelScope) { this@AndroidViewModel }
    }

    fun attachEventHandler(
        lifecycleOwner: LifecycleOwner,
        handler: EventHandler<Inputs, Events, State>
    ) {
        // events are sent back to the screen
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                impl.attachEventHandler(handler)
            }
        }
    }

    fun attachEventHandler(
        coroutineScope: CoroutineScope = impl.viewModelScope,
        handler: EventHandler<Inputs, Events, State>
    ) {
        coroutineScope.launch {
            impl.attachEventHandler(handler)
        }
    }
}
