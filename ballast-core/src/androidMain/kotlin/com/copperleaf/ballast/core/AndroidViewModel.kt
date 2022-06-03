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
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    public fun observeStatesOnLifecycle(
        lifecycleOwner: LifecycleOwner,
        targetState: Lifecycle.State = Lifecycle.State.RESUMED,
        onStateChanged: (State) -> Unit,
    ): Job = with(lifecycleOwner) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(targetState) {
                observeStates()
                    .onEach(onStateChanged)
                    .launchIn(this)
            }
        }
    }

    public fun attachEventHandlerOnLifecycle(
        lifecycleOwner: LifecycleOwner,
        handler: EventHandler<Inputs, Events, State>,
        targetState: Lifecycle.State = Lifecycle.State.RESUMED,
    ): Job = with(lifecycleOwner) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(targetState) {
                impl.attachEventHandler(handler)
            }
        }
    }

    /**
     * Deprecated in 1.2.0, scheduled to be removed in 2.0.0.
     */
    @Deprecated("replace with attachEventHandlerOnLifecycle", replaceWith = ReplaceWith("attachEventHandlerOnLifecycle(lifecycleOwner, handler)"))
    public fun attachEventHandler(
        lifecycleOwner: LifecycleOwner,
        handler: EventHandler<Inputs, Events, State>
    ) {
        attachEventHandlerOnLifecycle(lifecycleOwner, handler, Lifecycle.State.RESUMED)
    }

    public fun attachEventHandler(
        coroutineScope: CoroutineScope = impl.viewModelScope,
        handler: EventHandler<Inputs, Events, State>
    ): Job {
        return coroutineScope.launch {
            impl.attachEventHandler(handler)
        }
    }
}
