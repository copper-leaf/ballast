package com.copperleaf.ballast.analytics

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.awaitViewModelStart
import com.copperleaf.ballast.inputs
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

public class AnalyticsInterceptor<Inputs : Any, Events : Any, State : Any>(
    private val tracker: AnalyticsTracker,
    private val adapter: AnalyticsAdapter<Inputs, Events, State>,
) : BallastInterceptor<Inputs, Events, State> {

    public constructor(
        tracker: AnalyticsTracker,
        shouldTrackInput: (Inputs) -> Boolean
    ) : this(tracker, DefaultAnalyticsAdapter(shouldTrackInput))

    override fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.awaitViewModelStart()
            notifications
                .inputs {
                    it.filter { input ->
                        adapter.shouldTrackInput(input)
                    }
                }
                .onEach { input ->
                    tracker.trackAnalyticsEvent(
                        adapter.getEventIdForInput(input),
                        adapter.getEventParametersForInput(hostViewModelName, input),
                    )
                }
                .collect()
        }
    }
}
