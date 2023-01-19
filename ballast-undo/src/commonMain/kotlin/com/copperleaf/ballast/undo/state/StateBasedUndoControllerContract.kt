package com.copperleaf.ballast.undo.state

import com.copperleaf.ballast.BallastNotification
import kotlinx.coroutines.flow.Flow

public object StateBasedUndoControllerContract {
    public data class State<Inputs : Any, Events : Any, State : Any>(
        val latestState: State? = null,
        val frames: List<State> = emptyList(),
        val currentFrame: Int = -1,
    ) {
        val isUndoAvailable: Boolean = (currentFrame - 1) >= 0
        val isRedoAvailable: Boolean = (currentFrame + 1) <= frames.lastIndex
        val currentState: State? = frames.getOrNull(currentFrame)
    }

    public sealed class Inputs<Inputs : Any, Events : Any, State : Any> {
        public class ConnectViewModel<_Inputs : Any, Events : Any, State : Any>(
            public val notifications: Flow<BallastNotification<_Inputs, Events, State>>
        ) : Inputs<_Inputs, Events, State>()

        public class ConnectedStateChanged<_Inputs : Any, Events : Any, State : Any>(
            public val newState: State
        ) : Inputs<_Inputs, Events, State>()

        public class CaptureStateNow<_Inputs : Any, Events : Any, State : Any> : Inputs<_Inputs, Events, State>()

        public class Undo<_Inputs : Any, Events : Any, State : Any> : Inputs<_Inputs, Events, State>()
        public class Redo<_Inputs : Any, Events : Any, State : Any> : Inputs<_Inputs, Events, State>()
    }

    public sealed class Events<Inputs : Any, Events : Any, State : Any> {
        public class RestoreState<Inputs : Any, _Events : Any, State : Any>(
            public val stateToRestore: State,
        ) : Events<Inputs, _Events, State>()
    }
}
