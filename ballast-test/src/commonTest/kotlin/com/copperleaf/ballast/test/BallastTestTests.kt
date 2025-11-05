package com.copperleaf.ballast.test

import com.copperleaf.ballast.test.internal.BallastScenarioScopeImpl
import com.copperleaf.ballast.test.internal.TestInterceptor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
class BallastTestTests {

    @Test
    fun checkToStringValues() = runTest {
        assertEquals(
            "TestInterceptor", TestInterceptor<Any, Any, Any>(
                testCoroutineScope = this,
                onTestComplete = CompletableDeferred(),
                scenario = BallastScenarioScopeImpl(""),
                testSequenceTimeout = 1.seconds
            ).toString()
        )
    }
}
