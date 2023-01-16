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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.Closeable

public open class AndroidViewModel<Inputs : Any, Events : Any, State : Any>
private constructor(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    providedCoroutineScope: CoroutineScope?
) : ViewModel(
    *listOfNotNull(
        providedCoroutineScope.asCloseable()
    ).toTypedArray()
), BallastViewModel<Inputs, Events, State> by impl {

    @Deprecated(
        "Prefer passing a coroutineScope into this ViewModel externally. By default, Ballast will internally be " +
                "running on `viewModelScope` which is now discouraged because it has a hard dependency on " +
                "`Dispatchers.Main.immediate`. Deprecated since v3, but not currently scheduled for removal."
    )
    /**
     * Construct a new AndroidViewModel instance, which runs the Ballast processor on `viewModelScope`.
     *
     * This constructor is discouraged, prefer passing a coroutineScope into this ViewModel externally. By default,
     * Ballast will internally be running on `viewModelScope` which is now discouraged because it has a hard dependency
     * on `Dispatchers.Main.immediate`.
     *
     * <p>
     * You should <strong>never</strong> manually construct a ViewModel outside of a
     * {@link ViewModelProvider.Factory}.
     */
    public constructor(
        config: BallastViewModelConfiguration<Inputs, Events, State>,
    ) : this(
        impl = BallastViewModelImpl("AndroidViewModel", config),
        providedCoroutineScope = null,
    )

    /**
     * Construct a new AndroidViewModel instance, which runs the Ballast processor on [coroutineScope]. This
     * coroutineScope will be cancelled directly before {@link #onCleared()} is called.
     *
     * If [coroutineScope] also implements [Closeable], then the scope's own [Closeable.close] will be called when the
     * ViewModel is cleared. Otherwise, the scope will be wrapped in `Closeable { coroutineScope.cancel() }`.
     *
     * <p>
     * You should <strong>never</strong> manually construct a ViewModel outside of a
     * {@link ViewModelProvider.Factory}.
     */
    public constructor(
        config: BallastViewModelConfiguration<Inputs, Events, State>,
        coroutineScope: CoroutineScope,
    ) : this(
        impl = BallastViewModelImpl("AndroidViewModel", config),
        providedCoroutineScope = coroutineScope
    )

    init {
        // if a coroutineScope was provided through the constructor, use that. Otherwise, fall-back to using viewModelScope
        impl.start(providedCoroutineScope ?: viewModelScope)
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

    public fun runOnLifecycle(
        lifecycleOwner: LifecycleOwner,
        eventHandler: EventHandler<Inputs, Events, State>,
        targetState: Lifecycle.State = Lifecycle.State.RESUMED,
        onStateChanged: (State) -> Unit,
    ): Job = with(lifecycleOwner) {
        lifecycleScope.launch {
            joinAll(
                observeStatesOnLifecycle(lifecycleOwner, targetState, onStateChanged),
                attachEventHandlerOnLifecycle(lifecycleOwner, eventHandler, targetState),
            )
        }
    }

    public fun attachEventHandler(
        coroutineScope: CoroutineScope = impl.viewModelScope,
        handler: EventHandler<Inputs, Events, State>
    ): Job {
        return coroutineScope.launch {
            impl.attachEventHandler(handler)
        }
    }

    public companion object {
        private fun CoroutineScope?.asCloseable(): Closeable? {
            if (this == null) return null
            if (this is Closeable) return this

            return Closeable {
                this.cancel()
            }
        }
    }
}
