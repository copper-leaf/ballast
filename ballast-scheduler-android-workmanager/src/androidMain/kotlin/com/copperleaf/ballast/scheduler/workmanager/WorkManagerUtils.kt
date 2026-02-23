package com.copperleaf.ballast.scheduler.workmanager

import android.util.Log
import androidx.work.DirectExecutor
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.copperleaf.ballast.scheduler.Schedule
import com.copperleaf.ballast.scheduler.operators.getNext
import com.copperleaf.ballast.scheduler.workmanager.WorkManagerConstants.KEY_INPUT_DATA_PAYLOAD
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import kotlin.coroutines.resumeWithException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaDuration

public fun WorkManager.createSchedule(
    schedule: Schedule,
    callback: SchedulerCallback,
    json: Json = Json.Default,
    clock: Clock = Clock.System,
) {
    val scheduleData = BallastWorkManagerScheduleData(
        scheduleClassName = schedule::class.qualifiedName!!,
        callbackClassName = callback::class.qualifiedName!!,
    )
    val payloadJson = json.encodeToString(BallastWorkManagerScheduleData.serializer(), scheduleData)
    val runAt = schedule.getNext(clock.now())

    if (runAt == null) {
        Log.i("BallastWorkManager", "Schedule ${schedule::class.qualifiedName} has no next run time, skipping creation")
        return
    }

    val initialDelay = runAt - clock.now()

    val scheduleWorkRequest = OneTimeWorkRequestBuilder<BallastWorkManagerScheduleWorker>()
        .setInputData(workDataOf(KEY_INPUT_DATA_PAYLOAD to payloadJson))
        .addTag(scheduleData.scheduleClassName)
        .setInitialDelay(initialDelay.toJavaDuration())
        .build()

    this.beginUniqueWork(
        scheduleData.scheduleClassName,
        ExistingWorkPolicy.APPEND_OR_REPLACE,
        scheduleWorkRequest
    )
}

internal suspend fun WorkManager.updateExistingSchedule(
    scheduleData: BallastWorkManagerScheduleData,
    runAt: Instant,
    json: Json = Json.Default,
    clock: Clock = Clock.System,
) {
    // Retrieve the work request ID. In this example, the work being updated is unique
    // work so we can retrieve the ID using the unique work name.
    val existingWorkRequestId = this
        .getWorkInfosForUniqueWork(scheduleData.scheduleClassName)
        .await()
        .firstOrNull()
        ?.id ?: return

    // Create new WorkRequest from existing Worker, new constraints, and the id of the old WorkRequest.
    val payloadJson = json.encodeToString(BallastWorkManagerScheduleData.serializer(), scheduleData)
    val initialDelay = runAt - clock.now()
    val scheduleWorkRequest = OneTimeWorkRequestBuilder<BallastWorkManagerScheduleWorker>()
        .setInputData(workDataOf(KEY_INPUT_DATA_PAYLOAD to payloadJson))
        .addTag(scheduleData.scheduleClassName)
        .setInitialDelay(initialDelay.toJavaDuration())
        .setId(existingWorkRequestId)
        .build()

    // Pass the new WorkRequest to updateWork().
    this.updateWork(scheduleWorkRequest)
}

internal fun WorkManager.cancelSchedule(
    schedule: Schedule,
) {
}

public suspend fun <T> ListenableFuture<T>.await(): T {
    try {
        if (isDone) return getUninterruptibly(this)
    } catch (e: ExecutionException) {
        // ExecutionException is the only kind of exception that can be thrown from a gotten
        // Future, other than CancellationException. Cancellation is propagated upward so that
        // the coroutine running this suspend function may process it.
        // Any other Exception showing up here indicates a very fundamental bug in a
        // Future implementation.
        throw e.nonNullCause()
    }

    return suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
        addListener(ToContinuation(this, cont), DirectExecutor.INSTANCE)
        cont.invokeOnCancellation {
            cancel(false)
        }
    }
}

private fun <V> getUninterruptibly(future: Future<V>): V {
    var interrupted = false
    try {
        while (true) {
            try {
                return future.get()
            } catch (e: InterruptedException) {
                interrupted = true
            }
        }
    } finally {
        if (interrupted) {
            Thread.currentThread().interrupt()
        }
    }
}

private fun ExecutionException.nonNullCause(): Throwable {
    return this.cause!!
}

private class ToContinuation<T>(
    val futureToObserve: ListenableFuture<T>,
    val continuation: CancellableContinuation<T>,
) : Runnable {
    override fun run() {
        if (futureToObserve.isCancelled) {
            continuation.cancel()
        } else {
            try {
                continuation.resumeWith(Result.success(getUninterruptibly(futureToObserve)))
            } catch (e: ExecutionException) {
                // ExecutionException is the only kind of exception that can be thrown from a gotten
                // Future. Anything else showing up here indicates a very fundamental bug in a
                // Future implementation.
                continuation.resumeWithException(e.nonNullCause())
            }
        }
    }
}
