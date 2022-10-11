package com.copperleaf.ballast.undo

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.ExperimentalBallastApi
import kotlinx.coroutines.flow.Flow

/**
 * Adds undo/redo functionality to a Ballast ViewModel. This interceptor should be added to a ViewModel's configuration,
 * and only the [controller] needs to be accessed from the UI which is handling the undo/redo actions. An
 * [UndoController] should be created and managed separately from the ViewModel it is associated with, and should only
 * be associated with a single ViewModel.
 */
@ExperimentalBallastApi
public class BallastUndoInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val controller: UndoController<Inputs, Events, State>
) : BallastInterceptor<Inputs, Events, State> {

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

    override suspend fun onNotify(logger: BallastLogger, notification: BallastNotification<Inputs, Events, State>) {}
}
