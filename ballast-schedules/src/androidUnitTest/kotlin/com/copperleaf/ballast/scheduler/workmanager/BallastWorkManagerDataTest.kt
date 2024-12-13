package com.copperleaf.ballast.scheduler.workmanager

import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.SchedulerAdapterScope
import com.copperleaf.ballast.scheduler.schedule.EveryDaySchedule
import com.copperleaf.ballast.scheduler.workmanager.internal.getRegisteredSchedules
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toInstant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class BallastWorkManagerDataTest {
    @Test fun testDataClasses() = runTest {
        val usaCentralTimeZone = UtcOffset(ZoneOffset.ofHours(-6)).asTimeZone()
        val testAdapter = TestAdapter(usaCentralTimeZone)
        val testCallback = TestCallback()
        val workManagerData = BallastWorkManagerData(testAdapter, testCallback, false)
        assertEquals<Any?>(testAdapter, workManagerData.adapter)
        assertEquals<Any?>(
            "com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerDataTest\$TestAdapter",
            workManagerData.adapterClassName
        )
        assertEquals<Any?>(testCallback, workManagerData.callback)
        assertEquals<Any?>(
            "com.copperleaf.ballast.scheduler.workmanager.BallastWorkManagerDataTest\$TestCallback",
            workManagerData.callbackClassName
        )
        assertEquals<Any?>(false, workManagerData.withHistory)

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

        assertEquals<Any?>(workManagerData, scheduleData.workManagerData)
        assertEquals<Any?>(registeredSchedule, scheduleData.registeredSchedule)
        assertEquals<Any?>("Daily at 9am", scheduleData.key)
        assertEquals<Any?>(now, scheduleData.initialInstant)
        assertEquals<Any?>(now, scheduleData.latestInstant)
        assertEquals<Any?>(expectedNextTrigger, scheduleData.nextInstant)
        assertEquals<Any?>(9375000L.milliseconds, scheduleData.getDelayAmount(now))
        // (expectedNextTrigger - now)
    }

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
