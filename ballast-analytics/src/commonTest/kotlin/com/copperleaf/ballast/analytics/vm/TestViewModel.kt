package com.copperleaf.ballast.analytics.vm

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.analytics.AnalyticsInterceptor
import com.copperleaf.ballast.analytics.AnalyticsTracker
import com.copperleaf.ballast.analytics.DefaultAnalyticsAdapter
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.eventHandler
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
            interceptors += AnalyticsInterceptor(
                tracker = TestAnalyticsTracker(),

                // implement AnalyticsAdapter for full control over the eventId and eventParameters passed to the Tracker
                adapter = DefaultAnalyticsAdapter(
                    shouldTrackInput = { input ->
                        when (input) {
                            is TestContract.Inputs.TrackThis -> true
                            is TestContract.Inputs.DontTrackThis -> false
                        }
                    }
                )
            )
        }
        .build(),
    eventHandler = eventHandler { },
)

class TestAnalyticsTracker : AnalyticsTracker {
    override fun trackAnalyticsEvent(
        eventId: String,
        eventParameters: Map<String, String>
    ) {
        // TODO: track this event to your analytics SDK
    }
}
