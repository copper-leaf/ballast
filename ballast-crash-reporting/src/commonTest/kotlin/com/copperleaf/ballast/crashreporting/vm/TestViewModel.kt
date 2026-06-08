package com.copperleaf.ballast.crashreporting.vm

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.crashreporting.CrashReporter
import com.copperleaf.ballast.crashreporting.CrashReportingInterceptor
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
            interceptors += CrashReportingInterceptor(
                crashReporter = TestCrashReporter(),
                shouldTrackInput = { input ->
                    when (input) {
                        is TestContract.Inputs.TrackThis -> true
                        is TestContract.Inputs.DontTrackThis -> false
                    }
                }
            )
        }
        .build(),
    eventHandler = eventHandler { },
)

class TestCrashReporter : CrashReporter {
    override fun logInput(viewModelName: String, input: Any) {
        // log the event to your crash reporting system for trace of steps leading to a crash
    }

    override fun recordInputError(viewModelName: String, input: Any, throwable: Throwable) {
        // record the error caused when handling an Input
    }

    override fun recordEventError(viewModelName: String, event: Any, throwable: Throwable) {
        // record the error caused when handling an Input
    }

    override fun recordSideJobError(viewModelName: String, key: String, throwable: Throwable) {
        // record the error caused by a running SideJob
    }

    override fun recordUnhandledError(viewModelName: String, throwable: Throwable) {
        // record the error caused by something else (most likely out of your control)
    }
}
