package com.copperleaf.ballast.logging

import com.copperleaf.ballast.core.LoggingInterceptor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BallastLoggingTests {
    @Test
    fun checkToStringValues() = runTest {
        assertEquals(
            "LoggingInterceptor(enabled=[debug, info, error])",
            LoggingInterceptor<Any, Any, Any>().toString(),
        )
        assertEquals(
            "LoggingInterceptor(enabled=[info, error])",
            LoggingInterceptor<Any, Any, Any>(logDebug = false).toString(),
        )
        assertEquals(
            "LoggingInterceptor(enabled=[debug, error])",
            LoggingInterceptor<Any, Any, Any>(logInfo = false).toString(),
        )
        assertEquals(
            "LoggingInterceptor(enabled=[debug, info])",
            LoggingInterceptor<Any, Any, Any>(logError = false).toString(),
        )
    }
}
