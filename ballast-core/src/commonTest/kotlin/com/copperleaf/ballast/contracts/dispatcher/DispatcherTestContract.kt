package com.copperleaf.ballast.contracts.dispatcher

public object DispatcherTestContract {
    public data class State(
        val actualInputCoroutineScopeInfo: CoroutineScopeInfo? = null,
        val actualEventCoroutineScopeInfo: CoroutineScopeInfo? = null,
        val actualSideJobCoroutineScopeInfo: CoroutineScopeInfo? = null,
        val actualInterceptorCoroutineScopeInfo: CoroutineScopeInfo? = null,
    )

    public sealed class Inputs {
        data object Initialize : Inputs()

        data class SetEventDispatcher(val actualEventCoroutineScopeInfo: CoroutineScopeInfo?) : Inputs()
        data class SetSideJobDispatcher(val actualSideJobCoroutineScopeInfo: CoroutineScopeInfo?) : Inputs()
        data class SetInterceptorDispatcher(val actualInterceptorCoroutineScopeInfo: CoroutineScopeInfo?) : Inputs()
    }

    public sealed class Events {
        data object GetEventDispatcher : Events()
    }
}
