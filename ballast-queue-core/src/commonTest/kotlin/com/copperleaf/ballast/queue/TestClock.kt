@file:OptIn(ExperimentalCoroutinesApi::class)

package com.copperleaf.ballast.scheduler

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlin.time.Clock
import kotlin.time.Instant

private class TestScopeClock(private val testScope: TestScope) : Clock {
    override fun now(): Instant {
        return Instant.fromEpochMilliseconds(testScope.currentTime)
    }
}

fun TestScope.TestClock(startInstant: Instant? = null): Clock {
    val clock = TestScopeClock(this)
    startInstant?.let {
        advanceTimeBy(startInstant.toEpochMilliseconds())
    }
    return clock
}
