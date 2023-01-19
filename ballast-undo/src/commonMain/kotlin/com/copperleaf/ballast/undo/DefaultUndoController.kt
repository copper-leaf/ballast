package com.copperleaf.ballast.undo

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.states
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * A default, in-memory controller for handling undo/redo functionality by tracking State changes. The default mechanism
 * for capturing the States that can be restored to via undo/redo, is with [Flow.sample], with a sample rate of 5
 * seconds. You are free to provide your own sampling/buffering behavior by passing in a custom [bufferStates] lambda.
 *
 * To prevent long-running ViewModels from consuming too much memory, the states are managed in a circular array, where
 * at most [historyDepth] states will be stored in memory. Once the max length is hit, the oldest States will be purged
 * when new States are captured. The default history length is 10.
 */
@Deprecated(
    message = "Use StateBasedUndoController instead",
    replaceWith = ReplaceWith("StateBasedUndoController", "com.copperleaf.ballast.undo.state.StateBasedUndoController"),
)
public class DefaultUndoController<Inputs : Any, Events : Any, State : Any>(
    private val bufferStates: (Flow<State>) -> Flow<State> = { it.sample(5.seconds) },
    private val historyDepth: Int = 10,
) : UndoController<Inputs, Events, State> {
    public data class UndoControllerState<Inputs : Any, Events : Any, State : Any>(
        val frames: List<State> = emptyList(),
        val currentFrame: Int = -1,
    ) {
        val isUndoAvailable: Boolean = (currentFrame - 1) >= 0
        val isRedoAvailable: Boolean = (currentFrame + 1) <= frames.lastIndex
        val currentState: State? = frames.getOrNull(currentFrame)
    }

    private val undoState = MutableStateFlow(UndoControllerState<Inputs, Events, State>())
    private val restoreStateChannel = Channel<State>(Channel.RENDEZVOUS)
    private var undoRedoJob: Job? = null

    override val isUndoAvailable: Flow<Boolean> get() = undoState.map { it.isUndoAvailable }
    override val isRedoAvailable: Flow<Boolean> get() = undoState.map { it.isRedoAvailable }

    override fun undo() {
        val result = undoState.updateAndGet { it.copy(currentFrame = it.currentFrame - 1) }.currentState
        if (result != null) {
            restoreStateChannel.trySend(result)
        }
    }

    override fun redo() {
        val result = undoState.updateAndGet { it.copy(currentFrame = it.currentFrame + 1) }.currentState
        if (result != null) {
            restoreStateChannel.trySend(result)
        }
    }

    override fun UndoScope<Inputs, Events, State>.connectViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        undoRedoJob?.cancel()
        undoRedoJob = launch(start = CoroutineStart.UNDISPATCHED) {
            joinAll(
                observeStateChanges(notifications),
                restoreState(),
            )
        }
    }

    // as the ViewModel's state changes, buffer the actual states and when we get a net new state, save it to the
    private fun UndoScope<Inputs, Events, State>.observeStateChanges(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ): Job {
        return launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .states(bufferStates)
                .onEach { newFrame ->
                    undoState.updateAndGet { oldState ->
                        if (newFrame !in oldState.frames) {
                            // we have a completely new state, this is not one that has been restored
                            captureState(oldState, newFrame)
                        } else {
                            // we've already seen this state, it's being restored. Ignore this new state
                            oldState
                        }
                    }
                }
                .launchIn(this)
        }
    }

    // whenever someone calls undo or redo, send the corresponding state back to the ViewModel to restore the Ui to that
    // state
    private fun UndoScope<Inputs, Events, State>.restoreState(): Job {
        return launch(start = CoroutineStart.UNDISPATCHED) {
            restoreStateChannel
                .receiveAsFlow()
                .onEach { restoreState(it) }
                .launchIn(this)
        }
    }

    private fun captureState(
        oldState: UndoControllerState<Inputs, Events, State>,
        newFrame: State,
    ): UndoControllerState<Inputs, Events, State> {
        return if (oldState.frames.isEmpty()) {
            // this is the first capture we're taking, just do a simple add
            oldState.copy(
                frames = listOf(newFrame),
                currentFrame = 0,
            )
        } else if (oldState.frames.size == historyDepth && oldState.currentFrame == oldState.frames.lastIndex) {
            // we're at the max history, delete the first entry and shift everything down
            oldState.copy(
                frames = oldState.frames.drop(1) + newFrame,
                currentFrame = oldState.frames.lastIndex,
            )
        } else if (oldState.currentFrame == oldState.frames.lastIndex) {
            // we're appending a new state, but have not yet hit our max depth. Just do a simple addition
            oldState.copy(
                frames = oldState.frames + newFrame,
                currentFrame = oldState.frames.lastIndex + 1,
            )
        } else {
            // this is a subsequent capture, and we've done some undo and are in the middle of the list somewhere. Start
            // overwriting the entries in the frame stack, and clear everything after this index to avoid inconsistent
            // states
            oldState.copy(
                frames = oldState.frames.take(oldState.currentFrame + 1) + newFrame,
                currentFrame = oldState.currentFrame + 1,
            )
        }
    }
}
