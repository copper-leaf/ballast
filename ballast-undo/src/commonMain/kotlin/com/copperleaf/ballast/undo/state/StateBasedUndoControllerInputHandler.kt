package com.copperleaf.ballast.undo.state

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.observeFlows
import com.copperleaf.ballast.states
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class StateBasedUndoControllerInputHandler<Inputs : Any, Events : Any, State : Any>(
    private val bufferStates: (Flow<State>) -> Flow<State>,
    private val historyDepth: Int,
) : InputHandler<
        StateBasedUndoControllerContract.Inputs<Inputs, Events, State>,
        StateBasedUndoControllerContract.Events<Inputs, Events, State>,
        StateBasedUndoControllerContract.State<Inputs, Events, State>> {
    override suspend fun InputHandlerScope<
            StateBasedUndoControllerContract.Inputs<Inputs, Events, State>,
            StateBasedUndoControllerContract.Events<Inputs, Events, State>,
            StateBasedUndoControllerContract.State<Inputs, Events, State>>.handleInput(
        input: StateBasedUndoControllerContract.Inputs<Inputs, Events, State>
    ) = when (input) {
        is StateBasedUndoControllerContract.Inputs.ConnectViewModel -> {
            observeFlows(
                "ConnectViewModel",
                // collect all changes to the state, so we can later request capture
                input
                    .notifications
                    .states { it }
                    .map { newFrame ->
                        StateBasedUndoControllerContract.Inputs.ConnectedStateChanged(newFrame)
                    },

                // buffer the states to control when to actually capture the state. The captured state is the latest
                // emission of `ConnectedStateChanged` from the above Flow
                input
                    .notifications
                    .states(bufferStates)
                    .map {
                        StateBasedUndoControllerContract.Inputs.CaptureStateNow()
                    }
            )
        }

        is StateBasedUndoControllerContract.Inputs.ConnectedStateChanged -> {
            updateState { it.copy(latestState = input.newState) }
        }

        is StateBasedUndoControllerContract.Inputs.CaptureStateNow -> {
            updateState { oldState ->
                val newFrame = oldState.latestState
                if (newFrame == null) {
                    oldState
                } else if (newFrame !in oldState.frames) {
                    // we have a completely new state, this is not one that has been restored
                    captureState(oldState, newFrame)
                } else {
                    // we've already seen this state, it's being restored. Ignore this new state
                    oldState
                }
            }
        }

        is StateBasedUndoControllerContract.Inputs.Undo -> {
            val result = updateStateAndGet { it.copy(currentFrame = it.currentFrame - 1) }.currentState
            if (result != null) {
                postEvent(StateBasedUndoControllerContract.Events.RestoreState(result))
            } else {
                noOp()
            }
        }

        is StateBasedUndoControllerContract.Inputs.Redo -> {
            val result = updateStateAndGet { it.copy(currentFrame = it.currentFrame + 1) }.currentState
            if (result != null) {
                postEvent(StateBasedUndoControllerContract.Events.RestoreState(result))
            } else {
                noOp()
            }
        }
    }


    private fun captureState(
        oldState: StateBasedUndoControllerContract.State<Inputs, Events, State>,
        newFrame: State,
    ): StateBasedUndoControllerContract.State<Inputs, Events, State> {
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
