package com.copperleaf.ballast.scheduler.internal

import com.copperleaf.ballast.scheduler.NamedSchedule

internal class RegisteredSchedule<I : Any, E : Any, S : Any>(
    val schedule: NamedSchedule,
    val scheduledInput: () -> I,
)
