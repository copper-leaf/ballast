package com.copperleaf.ballast.undo

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.ExperimentalBallastApi
import kotlinx.coroutines.flow.Flow

/**
 * A generic interface for implementing undo/redo functionality. An UndoController is free to watch for changes to
 * either States or Inputs, but ultimately must handle "undo" by restoring the State to a particular point in time. It
 * also must be able to report whether the undo/redo actions are available as a [Flow].
 *
 * For a default, in-memory implementation that works by capturing States over time, see [DefaultUndoController]
 */
@ExperimentalBallastApi
public interface UndoController<Inputs : Any, Events : Any, State : Any> {
    public val isUndoAvailable: Flow<Boolean>
    public val isRedoAvailable: Flow<Boolean>

    public fun undo()
    public fun redo()

    public fun UndoScope<Inputs, Events, State>.connectViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    )
}
