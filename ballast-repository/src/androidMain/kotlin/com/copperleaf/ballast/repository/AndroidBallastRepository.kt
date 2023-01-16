package com.copperleaf.ballast.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.repository.bus.EventBus
import com.copperleaf.ballast.repository.bus.EventBusEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.Closeable

/**
 * An implementation of [BallastRepository] built on top of the standard Android ViewModel, so that the repository can
 * be tied more properly into the Android Application lifecycle, or be scoped to NavGraphs if needed.
 */
public open class AndroidBallastRepository<Inputs : Any, State : Any>
private constructor(
    private val impl: BallastViewModelImpl<Inputs, Any, State>,
    private val eventBus: EventBus,
    providedCoroutineScope: CoroutineScope?
) : ViewModel(
    *listOfNotNull(
        providedCoroutineScope.asCloseable()
    ).toTypedArray()
), BallastViewModel<Inputs, Any, State> by impl {

    @Deprecated(
        "Prefer passing a coroutineScope into this ViewModel externally. By default, Ballast will internally be " +
                "running on `viewModelScope` which is now discouraged because it has a hard dependency on " +
                "`Dispatchers.Main.immediate`. Deprecated since v3, but not currently scheduled for removal."
    )
    /**
     * Construct a new AndroidBallastRepository instance, which runs the Ballast processor on `viewModelScope`.
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
        eventBus: EventBus,
        config: BallastViewModelConfiguration<Inputs, Any, State>,
    ) : this(
        impl = BallastViewModelImpl("AndroidBallastRepository", config),
        eventBus = eventBus,
        providedCoroutineScope = null,
    )

    @Deprecated(
        "Prefer passing a coroutineScope into this ViewModel externally. By default, Ballast will internally be " +
                "running on `viewModelScope` which is now discouraged because it has a hard dependency on " +
                "`Dispatchers.Main.immediate`. Deprecated since v3, but not currently scheduled for removal."
    )
    /**
     * Construct a new AndroidBallastRepository instance, which runs the Ballast processor on [coroutineScope]. This
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
        eventBus: EventBus,
        config: BallastViewModelConfiguration<Inputs, Any, State>,
        coroutineScope: CoroutineScope,
    ) : this(
        impl = BallastViewModelImpl("AndroidBallastRepository", config),
        eventBus = eventBus,
        providedCoroutineScope = coroutineScope,
    )

    init {
        impl.start(viewModelScope)
        viewModelScope.launch {
            impl.attachEventHandler(EventBusEventHandler(eventBus))
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
