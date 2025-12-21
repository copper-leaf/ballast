package com.copperleaf.ballast.scheduler

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlin.time.Clock
import kotlin.time.Instant

private class TestScopeClock(private val testScope: TestScope) : Clock {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun now(): Instant {
        return Instant.fromEpochMilliseconds(testScope.currentTime)
    }
}

fun TestScope.TestClock(): Clock = TestScopeClock(this)
