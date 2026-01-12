package com.copperleaf.ballast.scheduler.docs

import com.copperleaf.ballast.scheduler.executor.DelayScheduleExecutor
import com.copperleaf.ballast.scheduler.operators.named
import com.copperleaf.ballast.scheduler.schedule.EveryMinuteSchedule
import com.copperleaf.ballast.scheduler.schedule.EverySecondSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DocsSnippets {

    val viewModelScope: CoroutineScope = TODO()

    fun snippet1() {
        val schedule = EveryMinuteSchedule()
        val executor = DelayScheduleExecutor()

        executor
            .runSchedule(schedule)
            .onEach {
                println("Executing scheduled task at ${it.triggeredAt}")
            }
            .launchIn(viewModelScope)
    }

    fun snippet2() {
        val schedule1 = EveryMinuteSchedule().named("EveryMinuteSchedule")
        val schedule2 = EverySecondSchedule().named("EverySecondSchedule")
        val executor = DelayScheduleExecutor()

        executor
            .runSchedules(listOf(schedule1, schedule2))
            .onEach {
                println("Executing scheduled task from ${it.name} at ${it.triggeredAt}")
            }
            .launchIn(viewModelScope)
    }
}
