package com.copperleaf.ballast.undo

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import kotlinx.coroutines.flow.Flow

/**
 * Adds undo/redo functionality to a Ballast ViewModel. This interceptor should be added to a ViewModel's configuration,
 * and only the [controller] needs to be accessed from the UI which is handling the undo/redo actions. An
 * [UndoController] should be created and managed separately from the ViewModel it is associated with, and should only
 * be associated with a single ViewModel.
 */
public class BallastUndoInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val controller: UndoController<Inputs, Events, State>
) : BallastInterceptor<Inputs, Events, State>, UndoController<Inputs, Events, State> by controller {

    public object Key : BallastInterceptor.Key<BallastUndoInterceptor<*, *, *>>
    override val key: BallastInterceptor.Key<BallastUndoInterceptor<*, *, *>> = BallastUndoInterceptor.Key

    override fun BallastInterceptorScope<Inputs, Events, State>.start(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        val scope = UndoScopeImpl(this)
        with(controller) {
            with(scope) {
                connectViewModel(
                    notifications = notifications,
                )
            }
        }
    }
}
