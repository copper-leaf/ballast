package com.copperleaf.ballast.analytics.vm

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.firebase.FirebaseAnalyticsInterceptor
import com.copperleaf.ballast.firebase.FirebaseAnalyticsTrackInput
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

class TestViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State
        >(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(TestContract.State(), TestInputHandler())
        .apply {
            interceptors += FirebaseAnalyticsInterceptor()
        }
        .build(),
    eventHandler = eventHandler { },
)

object TestContract {
    data class State(
        val loading: Boolean = false,
    )

    sealed interface Inputs {

        @FirebaseAnalyticsTrackInput
        data object TrackThis : Inputs

        data object DontTrackThis : Inputs
    }

    sealed interface Events
}
