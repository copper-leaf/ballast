package com.copperleaf.ballast.test

import com.copperleaf.ballast.test.internal.BallastScenarioScopeImpl
import com.copperleaf.ballast.test.internal.TestInterceptor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
class BallastTestTests : StringSpec({
    "check toString values" {
        TestInterceptor<Any, Any, Any>(
            testCoroutineScope = this,
            onTestComplete = CompletableDeferred(),
            scenario = BallastScenarioScopeImpl(""),
            testSequenceTimeout = 1.seconds
        ).toString() shouldBe "TestInterceptor"
    }
})
