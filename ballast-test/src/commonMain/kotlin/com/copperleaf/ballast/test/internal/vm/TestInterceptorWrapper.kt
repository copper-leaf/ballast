package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModel

/**
 * This class wraps a standard Interceptor to pull Inputs out of the TestViewModel wrapped INput type, and convert it
 * back to the intended Input that the test is running against.
 */
internal class TestInterceptorWrapper<Inputs : Any, Events : Any, State : Any>(
    private val delegate: BallastInterceptor<Inputs, Events, State>,
) : BallastInterceptor<TestViewModel.Inputs<Inputs>, Events, State> {

    private var startedViewModelType: String? = null
    private var startedViewModelName: String? = null
    private var startedViewModelWrapped: BallastViewModel<Inputs, Events, State>? = null

    private suspend inline fun TestViewModel.Inputs<Inputs>.unwrap(
        logger: BallastLogger,
        block: (Inputs) -> BallastNotification<Inputs, Events, State>
    ) {
        when (this) {
            is TestViewModel.Inputs.AwaitInput -> {
                block(this.normalInput).let { delegate.onNotify(logger, it) }
            }
            is TestViewModel.Inputs.ProcessInput -> {
                block(this.normalInput).let { delegate.onNotify(logger, it) }
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun onNotify(
        logger: BallastLogger,
        notification: BallastNotification<TestViewModel.Inputs<Inputs>, Events, State>
    ) {
        when (notification) {
            is BallastNotification.ViewModelStarted -> {
                startedViewModelType = notification.viewModelType
                startedViewModelName = notification.viewModelName
                delegate.onNotify(logger, BallastNotification.ViewModelStarted(notification.viewModelType, notification.viewModelName))
            }
            is BallastNotification.ViewModelCleared -> {
                check(notification.viewModelType === startedViewModelType)
                check(notification.viewModelName === startedViewModelName)
                delegate.onNotify(logger, BallastNotification.ViewModelCleared(notification.viewModelType, notification.viewModelName))
            }

            is BallastNotification.InputAccepted -> {
                notification.input.unwrap(logger) {
                    BallastNotification.InputAccepted(notification.viewModelType, notification.viewModelName, it)
                }
            }
            is BallastNotification.InputRejected -> {
                notification.input.unwrap(logger) {
                    BallastNotification.InputRejected(notification.viewModelType, notification.viewModelName, notification.stateWhenRejected, it)
                }
            }
            is BallastNotification.InputDropped -> {
                notification.input.unwrap(logger) {
                    BallastNotification.InputDropped(notification.viewModelType, notification.viewModelName, it)
                }
            }
            is BallastNotification.InputHandledSuccessfully -> {
                notification.input.unwrap(logger) {
                    BallastNotification.InputHandledSuccessfully(notification.viewModelType, notification.viewModelName, it)
                }
            }
            is BallastNotification.InputCancelled -> {
                notification.input.unwrap(logger) {
                    BallastNotification.InputCancelled(notification.viewModelType, notification.viewModelName, it)
                }
            }
            is BallastNotification.InputHandlerError -> {
                notification.input.unwrap(logger) {
                    BallastNotification.InputHandlerError(notification.viewModelType, notification.viewModelName, it, notification.throwable)
                }
            }

            else -> {
                // should be safe to cast, since none of the properties of the remaining Notifications include an
                //    Input parameter, which is the only type that has changed from this to the delegate
                delegate.onNotify(logger, notification as BallastNotification<Inputs, Events, State>)
            }
        }
    }
}
