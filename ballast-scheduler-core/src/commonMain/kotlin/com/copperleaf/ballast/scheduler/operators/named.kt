package com.copperleaf.ballast.scheduler.operators

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.Schedule

public fun Schedule.named(name: String): NamedSchedule {
    return NamedScheduleImpl(name, this)
}

private class NamedScheduleImpl(
    override val name: String,
    private val scheduleDelegate: Schedule,
) : NamedSchedule, Schedule by scheduleDelegate
