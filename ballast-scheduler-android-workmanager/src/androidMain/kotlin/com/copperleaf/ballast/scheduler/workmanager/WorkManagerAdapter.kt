package com.copperleaf.ballast.scheduler.workmanager

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.copperleaf.ballast.scheduler.NamedSchedule
import com.copperleaf.ballast.scheduler.SchedulerCallback
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleData
import com.copperleaf.ballast.scheduler.executor.event.EventDrivenScheduleExecutor
import com.copperleaf.ballast.scheduler.workmanager.WorkManagerConstants.BALLAST_TAG
import com.copperleaf.ballast.scheduler.workmanager.WorkManagerConstants.KEY_INPUT_DATA_PAYLOAD
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.toJavaDuration

public class WorkManagerAdapter<S : NamedSchedule, C : SchedulerCallback>(
    private val workManager: WorkManager,
    private val clock: Clock = Clock.System,
    private val json: Json = Json.Default,
    private val constraints: Constraints = Constraints.NONE,
) : EventDrivenScheduleExecutor.Adapter {
    private companion object {
        const val TAG = "WorkManagerAdapter"
    }

    override suspend fun registerSchedule(data: EventDrivenScheduleData) {
        Log.i(TAG, "Registering schedule '${data.scheduleUniqueName}' with WorkManager for execution at ${data.nextExecution}")
        val initialDelay = data.nextExecution - clock.now()
        val dataJson = json.encodeToString(EventDrivenScheduleData.serializer(), data)

        val scheduleWorkRequest = OneTimeWorkRequestBuilder<BallastWorkManagerScheduleWorker<S, C>>()
            .setInputData(workDataOf(KEY_INPUT_DATA_PAYLOAD to dataJson))
            .setInitialDelay(initialDelay.toJavaDuration())
            .setConstraints(constraints)
            .addTag(BALLAST_TAG)
            .addTag(data.scheduleUniqueName)
            .build()

        workManager
            .beginUniqueWork(
                data.scheduleUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                scheduleWorkRequest
            )
            .enqueue()
            .await()
    }

    override suspend fun updateSchedule(data: EventDrivenScheduleData) {
        Log.i(TAG, "Updating schedule '${data.scheduleUniqueName}' with WorkManager for execution at ${data.nextExecution}")
        val existingWorkRequestId = workManager
            .getWorkInfosForUniqueWork(data.scheduleUniqueName)
            .await()
            .firstOrNull()
            ?.id ?: return

        val initialDelay = data.nextExecution - clock.now()
        val dataJson = json.encodeToString(EventDrivenScheduleData.serializer(), data)

        val scheduleWorkRequest = OneTimeWorkRequestBuilder<BallastWorkManagerScheduleWorker<S, C>>()
            .setInputData(workDataOf(KEY_INPUT_DATA_PAYLOAD to dataJson))
            .setInitialDelay(initialDelay.toJavaDuration())
            .setConstraints(constraints)
            .addTag(BALLAST_TAG)
            .addTag(data.scheduleUniqueName)
            .build()

        workManager
            .beginUniqueWork(
                data.scheduleUniqueName,
                ExistingWorkPolicy.REPLACE,
                scheduleWorkRequest
            )
            .enqueue()
            .await()
    }

    override suspend fun cancelSchedule(data: EventDrivenScheduleData) {
        Log.i(TAG, "Cancelling schedule '${data.scheduleUniqueName}' with WorkManager")
        val existingWorkRequestId = workManager
            .getWorkInfosForUniqueWork(data.scheduleUniqueName)
            .await()
            .firstOrNull()
            ?.id ?: return

        workManager
            .cancelWorkById(existingWorkRequestId)
            .await()
    }

    override suspend fun synchronizeSchedules(schedules: Sequence<EventDrivenScheduleData>) {
        // No-op, since WorkManager will persist the work across app restarts, so we don't need to do anything here
    }
}
