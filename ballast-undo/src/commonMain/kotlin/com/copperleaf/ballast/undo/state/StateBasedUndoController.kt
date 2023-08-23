package com.copperleaf.ballast.undo.state

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.undo.UndoController
import com.copperleaf.ballast.undo.UndoScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

/**
 * A default, in-memory controller for handling undo/redo functionality by tracking State changes. The default mechanism
 * for capturing the States that can be restored to via undo/redo, is with [Flow.sample], with a sample rate of 5
 * seconds. You are free to provide your own sampling/buffering behavior by passing in a custom [bufferStates] lambda to
 * [withStateBasedUndoController].
 *
 * To prevent long-running ViewModels from consuming too much memory, the states are managed in a circular array, where
 * at most [historyDepth] states will be stored in memory. Once the max length is hit, the oldest States will be purged
 * when new States are captured. The default history length is 10.
 */
public class StateBasedUndoController<Inputs : Any, Events : Any, State : Any> internal constructor(
    private val impl: BallastViewModelImpl<
            StateBasedUndoControllerContract.Inputs<Inputs, Events, State>,
            StateBasedUndoControllerContract.Events<Inputs, Events, State>,
            StateBasedUndoControllerContract.State<Inputs, Events, State>>
) : BallastViewModel<
        StateBasedUndoControllerContract.Inputs<Inputs, Events, State>,
        StateBasedUndoControllerContract.Events<Inputs, Events, State>,
        StateBasedUndoControllerContract.State<Inputs, Events, State>> by impl,
    UndoController<Inputs, Events, State> {

    public constructor(
        config: BallastViewModelConfiguration<
                StateBasedUndoControllerContract.Inputs<Inputs, Events, State>,
                StateBasedUndoControllerContract.Events<Inputs, Events, State>,
                StateBasedUndoControllerContract.State<Inputs, Events, State>> = BallastViewModelConfiguration.Builder()
            .withStateBasedUndoController<Inputs, Events, State>()
            .build(),
    ) : this(
        impl = BallastViewModelImpl("StateBasedUndoController", config),
    )

    override val isUndoAvailable: Flow<Boolean> get() = observeStates().map { it.isUndoAvailable }
    override val isRedoAvailable: Flow<Boolean> get() = observeStates().map { it.isRedoAvailable }

    override fun undo() {
        trySend(StateBasedUndoControllerContract.Inputs.Undo())
    }

    override fun redo() {
        trySend(StateBasedUndoControllerContract.Inputs.Redo())
    }

    public fun captureNow() {
        trySend(StateBasedUndoControllerContract.Inputs.CaptureStateNow())
    }

    override fun UndoScope<Inputs, Events, State>.connectViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        impl.start(this)
        launch {
            impl.attachEventHandler(StateBasedUndoControllerEventHandler(this@connectViewModel))
        }
        launch {
            send(StateBasedUndoControllerContract.Inputs.ConnectViewModel(notifications))
        }
    }

    override fun toString(): String {
        return "StateBasedUndoController"
    }
}
