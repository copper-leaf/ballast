package com.copperleaf.ballast.logging

import com.copperleaf.ballast.core.LoggingInterceptor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BallastLoggingTests : StringSpec({
    "check toString values" {
        LoggingInterceptor<Any, Any, Any>().toString() shouldBe "LoggingInterceptor(enabled=[debug, info, error])"
        LoggingInterceptor<Any, Any, Any>(logDebug = false).toString() shouldBe "LoggingInterceptor(enabled=[info, error])"
        LoggingInterceptor<Any, Any, Any>(logInfo = false).toString() shouldBe "LoggingInterceptor(enabled=[debug, error])"
        LoggingInterceptor<Any, Any, Any>(logError = false).toString() shouldBe "LoggingInterceptor(enabled=[debug, info])"
    }
})
