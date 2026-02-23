package com.copperleaf.ballast.scheduler.executor.event

import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.operators.getNext
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Clock
import kotlin.time.Instant

public class EventDrivenScheduleExecutor<S : NamedSchedule, C : SchedulerCallback>(
    private val adapter: EventDrivenScheduleExecutor.Adapter,
    private val state: EventDrivenScheduleExecutor.State,
    private val scheduleSerializer: KSerializer<S>,
    private val callbackSerializer: KSerializer<C>,
    public val json: Json = Json.Default,
    private val clock: Clock = Clock.System,
) {
    public suspend fun registerSchedule(schedule: S, callback: C) {
        val existingScheduleState = state.getState(schedule.name)

        if (existingScheduleState != null) {
            error("Schedule ${schedule.name} already exists, cannot be created")
        }
        val next = schedule.getNext(clock.now()) ?: return

        val newScheduleState = EventDrivenScheduleData(
            scheduleUniqueName = schedule.name,
            scheduleJson = json.encodeToJsonElement(scheduleSerializer, schedule) as JsonObject,
            callbackJson = json.encodeToJsonElement(callbackSerializer, callback) as JsonObject,
            lastExecution = null,
            nextExecution = next
        )

        try {
            adapter.registerSchedule(newScheduleState)
            state.storeScheduleData(newScheduleState)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public suspend fun registerOrUpdateSchedule(schedule: S, callback: C) {
        val existingScheduleState = state.getState(schedule.name)

        val updatedScheduleState = if (existingScheduleState == null) {
            val next = schedule.getNext(clock.now()) ?: return

            EventDrivenScheduleData(
                scheduleUniqueName = schedule.name,
                scheduleJson = json.encodeToJsonElement(scheduleSerializer, schedule) as JsonObject,
                callbackJson = json.encodeToJsonElement(callbackSerializer, callback) as JsonObject,
                lastExecution = null,
                nextExecution = next
            )
        } else {
            existingScheduleState
        }

        try {
            if (existingScheduleState == null) {
                adapter.registerSchedule(updatedScheduleState)
            } else {
                adapter.updateSchedule(updatedScheduleState)
            }
            state.storeScheduleData(updatedScheduleState)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public suspend fun updateSchedule(schedule: S, lastExecution: Instant, next: Instant) {
        val existingScheduleState = state.getState(schedule.name)
            ?: error("Schedule ${schedule.name} doesn't exist, cannot be updated")
        val updatedScheduleState = existingScheduleState.copy(
            lastExecution = lastExecution,
            nextExecution = next
        )

        try {
            adapter.updateSchedule(updatedScheduleState)
            state.storeScheduleData(updatedScheduleState)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public suspend fun cancelSchedule(schedule: S) {
        val existingScheduleState = state.getState(schedule.name)
            ?: error("Schedule ${schedule.name} doesn't exist, cannot be cancelled")

        try {
            adapter.cancelSchedule(existingScheduleState)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            state.removeScheduleData(schedule.name)
        }
    }

    public suspend fun synchronizeSchedules() {
        adapter.synchronizeSchedules(state.getAllSchedules())
    }

    public suspend fun handleTask(data: EventDrivenScheduleData) {
        val now = clock.now()
        dispatchWork(data)
        enqueueNextTask(now, data)
    }

// Helpers
// ---------------------------------------------------------------------------------------------------------------------

    private suspend fun dispatchWork(data: EventDrivenScheduleData) {
        val callback = json.decodeFromJsonElement(callbackSerializer, data.callbackJson)
        callback.handleTask()
    }

    private suspend fun enqueueNextTask(now: Instant, data: EventDrivenScheduleData) {
        val schedule = json.decodeFromJsonElement(scheduleSerializer, data.scheduleJson)
        val next = schedule.getNext(now)

        if (next != null) {
            updateSchedule(schedule, now, next)
        } else {
            cancelSchedule(schedule)
        }
    }

    public interface State {
        public suspend fun getAllSchedules(): Sequence<EventDrivenScheduleData>

        public suspend fun getState(scheduleUniqueName: String): EventDrivenScheduleData?

        public suspend fun storeScheduleData(data: EventDrivenScheduleData)

        public suspend fun removeScheduleData(scheduleUniqueName: String)
    }

    public interface Adapter {
        public suspend fun registerSchedule(data: EventDrivenScheduleData)

        public suspend fun updateSchedule(data: EventDrivenScheduleData)

        public suspend fun cancelSchedule(data: EventDrivenScheduleData)

        public suspend fun synchronizeSchedules(schedules: Sequence<EventDrivenScheduleData>)
    }
}
