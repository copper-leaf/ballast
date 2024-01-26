package com.copperleaf.ballast.scheduler.workmanager

import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.SchedulerAdapterScope
import com.copperleaf.ballast.scheduler.schedule.EveryDaySchedule
import com.copperleaf.ballast.scheduler.workmanager.internal.getRegisteredSchedules
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toInstant
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.milliseconds

class BallastWorkManagerDataTest : StringSpec({
    "Test data classes" {
        val usaCentralTimeZone = UtcOffset(ZoneOffset.ofHours(-6)).asTimeZone()
        val testAdapter = TestAdapter(usaCentralTimeZone)
        val testCallback = TestCallback()
        val workManagerData = BallastWorkManagerData(testAdapter, testCallback, false)
        workManagerData.adapter shouldBe testAdapter
        workManagerData.adapterClassName shouldBe "com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerDataTest\$TestAdapter"
        workManagerData.callback shouldBe testCallback
        workManagerData.callbackClassName shouldBe "com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerDataTest\$TestCallback"
        workManagerData.withHistory shouldBe false

        val registeredSchedule = workManagerData.adapter.getRegisteredSchedules().single()

        val now = LocalDateTime(
            LocalDate(2024, Month.FEBRUARY, 6),
            LocalTime(6, 23, 45),
        ).toInstant(usaCentralTimeZone)
        val expectedNextTrigger = LocalDateTime(
            LocalDate(2024, Month.FEBRUARY, 6),
            LocalTime(9, 0, 0),
        ).toInstant(usaCentralTimeZone)
        val scheduleData = BallastWorkScheduleData(
            workManagerData = workManagerData,
            registeredSchedule = registeredSchedule,
            initialInstant = now,
            latestInstant = now,
        )

        scheduleData.workManagerData shouldBe workManagerData
        scheduleData.registeredSchedule shouldBe registeredSchedule
        scheduleData.key shouldBe "Daily at 9am"
        scheduleData.initialInstant shouldBe now
        scheduleData.latestInstant shouldBe now
        scheduleData.nextInstant shouldBe expectedNextTrigger
        scheduleData.getDelayAmount(now) shouldBe 9375000L.milliseconds // (expectedNextTrigger - now)
    }
}) {
    class TestAdapter(private val timeZone: TimeZone) : SchedulerAdapter<Unit, Unit, Unit> {
        override suspend fun SchedulerAdapterScope<Unit, Unit, Unit>.configureSchedules() {
            onSchedule(
                key = "Daily at 9am",
                schedule = EveryDaySchedule(LocalTime(9, 0), timeZone = timeZone),
                scheduledInput = { }
            )
        }
    }

    class TestCallback : SchedulerCallback<Unit> {
        override suspend fun dispatchInput(input: Unit) {
            // no-op
        }
    }
}
