package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModel

/**
 * This class wraps a standard Interceptor to pull Inputs out of the TestViewModel wrapped INput type, and convert it
 * back to the intended Input that the test is running against.
 */
internal class TestInterceptorWrapper<Inputs : Any, Events : Any, State : Any>(
    private val delegate: BallastInterceptor<Inputs, Events, State>,
) : BallastInterceptor<TestViewModel.Inputs<Inputs>, Events, State> {

    private var startedViewModel: BallastViewModel<TestViewModel.Inputs<Inputs>, Events, State>? = null
    private var startedViewModelWrapped: BallastViewModel<Inputs, Events, State>? = null

    private suspend inline fun TestViewModel.Inputs<Inputs>.unwrap(
        block: (Inputs) -> BallastNotification<Inputs, Events, State>
    ) {
        when (this) {
            is TestViewModel.Inputs.AwaitInput -> {
                block(this.normalInput).let { delegate.onNotify(it) }
            }
            is TestViewModel.Inputs.ProcessInput -> {
                block(this.normalInput).let { delegate.onNotify(it) }
            }
            is TestViewModel.Inputs.TestCompleted -> {
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun onNotify(notification: BallastNotification<TestViewModel.Inputs<Inputs>, Events, State>) {
        when (notification) {
            is BallastNotification.ViewModelStarted -> {
                startedViewModel = notification.vm
                startedViewModelWrapped = ViewModelWrapper(startedViewModel!!)
                delegate.onNotify(BallastNotification.ViewModelStarted(startedViewModelWrapped!!))
            }
            is BallastNotification.ViewModelCleared -> {
                check(notification.vm === startedViewModel)
                delegate.onNotify(BallastNotification.ViewModelCleared(startedViewModelWrapped!!))
                startedViewModel = null
                startedViewModelWrapped = null
            }

            is BallastNotification.InputAccepted -> {
                notification.input.unwrap {
                    BallastNotification.InputAccepted(startedViewModelWrapped!!, it)
                }
            }
            is BallastNotification.InputRejected -> {
                notification.input.unwrap {
                    BallastNotification.InputRejected(startedViewModelWrapped!!, notification.stateWhenRejected, it)
                }
            }
            is BallastNotification.InputDropped -> {
                notification.input.unwrap {
                    BallastNotification.InputDropped(startedViewModelWrapped!!, it)
                }
            }
            is BallastNotification.InputHandledSuccessfully -> {
                notification.input.unwrap {
                    BallastNotification.InputHandledSuccessfully(startedViewModelWrapped!!, it)
                }
            }
            is BallastNotification.InputCancelled -> {
                notification.input.unwrap {
                    BallastNotification.InputCancelled(startedViewModelWrapped!!, it)
                }
            }
            is BallastNotification.InputHandlerError -> {
                notification.input.unwrap {
                    BallastNotification.InputHandlerError(startedViewModelWrapped!!, it, notification.throwable)
                }
            }

            else -> {
                // should be safe to cast, since none of the properties of the remaining Notifications include an
                //    Input parameter, which is the only type that has changed from this to the delegate
                delegate.onNotify(notification as BallastNotification<Inputs, Events, State>)
            }
        }
    }
}
