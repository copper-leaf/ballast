package com.copperleaf.ballast.contracts.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

public object DispatcherTestContract {
    public data class State(
        val actualInputDispatcher: CoroutineDispatcher? = null,
        val actualEventDispatcher: CoroutineDispatcher? = null,
        val actualSideJobDispatcher: CoroutineDispatcher? = null,
        val actualInterceptorDispatcher: CoroutineDispatcher? = null,
    )

    public sealed class Inputs {
        object Initialize : Inputs()

        data class SetEventDispatcher(val actualEventDispatcher: CoroutineDispatcher?) : Inputs()
        data class SetSideJobDispatcher(val actualSideJobDispatcher: CoroutineDispatcher?) : Inputs()
        data class SetInterceptorDispatcher(val actualInterceptorDispatcher: CoroutineDispatcher?) : Inputs()
    }

    public sealed class Events {
        object GetEventDispatcher : Events()
    }
}
