package com.copperleaf.ballast.crashreporting

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BallastCrashReportingTests : StringSpec({
    "check toString values" {
        CrashReportingInterceptor<Any, Any, Any>(
            crashReporter = TestCrashReporter(),
            shouldTrackInput = { true },
        ).toString() shouldBe "CrashReportingInterceptor(crashReporter=TestCrashReporter)"
    }
})

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
