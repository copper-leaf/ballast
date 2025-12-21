package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.Schedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public fun Schedule.filterByDayOfWeek(vararg daysOfWeek: DayOfWeek, timeZone: TimeZone = TimeZone.UTC): Schedule {
    return transformSchedule { scheduleSequence ->
        scheduleSequence
            .filter {
                val localDateTime = it.toLocalDateTime(timeZone)
                localDateTime.dayOfWeek in daysOfWeek
            }
    }
}

public fun Schedule.weekdays(timeZone: TimeZone = TimeZone.UTC): Schedule {
    return filterByDayOfWeek(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        timeZone = timeZone,
    )
}

public fun Schedule.weekends(timeZone: TimeZone = TimeZone.UTC): Schedule {
    return filterByDayOfWeek(
        DayOfWeek.SUNDAY,
        DayOfWeek.SATURDAY,
        timeZone = timeZone,
    )
}
