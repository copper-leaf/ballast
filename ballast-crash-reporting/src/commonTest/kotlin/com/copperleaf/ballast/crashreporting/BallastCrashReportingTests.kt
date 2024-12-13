package com.copperleaf.ballast.crashreporting

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BallastCrashReportingTests {
    @Test
    fun checkToStringValues() = runTest {
        assertEquals<Any?>(
            "CrashReportingInterceptor(crashReporter=TestCrashReporter)", CrashReportingInterceptor<Any, Any, Any>(
                crashReporter = TestCrashReporter(),
                shouldTrackInput = { true },
            ).toString()
        )
    }
}

private class TestCrashReporter : CrashReporter {
    override fun logInput(viewModelName: String, input: Any) {
        TODO("Not yet implemented")
    }

    override fun recordInputError(viewModelName: String, input: Any, throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun recordEventError(viewModelName: String, event: Any, throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun recordSideJobError(viewModelName: String, key: String, throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun recordUnhandledError(viewModelName: String, throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "TestCrashReporter"
    }
}
