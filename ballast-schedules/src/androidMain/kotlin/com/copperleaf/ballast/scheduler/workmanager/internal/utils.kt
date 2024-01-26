package com.copperleaf.ballast.scheduler.workmanager.internal

import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.internal.RegisteredSchedule
import com.copperleaf.ballast.scheduler.internal.SchedulerAdapterScopeImpl
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Internal utils
// ---------------------------------------------------------------------------------------------------------------------

internal suspend fun <I : Any, E : Any, S : Any> SchedulerAdapter<I, E, S>.getRegisteredSchedules(): List<RegisteredSchedule<*, *, *>> {
    val adapter = this
    val adapterScope = SchedulerAdapterScopeImpl<I, E, S>()

    with(adapter) {
        adapterScope.configureSchedules()
    }

    // cancel any running schedules which have the same keys as the newly requested schedules
    return adapterScope.schedules
}

@Suppress("BlockingMethodInNonBlockingContext", "RedundantSamConstructor")
internal suspend inline fun <R : Any> ListenableFuture<R>.awaitInternal(): R {
    // Fast path
    if (isDone) {
        try {
            return get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
    return suspendCancellableCoroutine { cancellableContinuation ->
        addListener(
            Runnable {
                try {
                    cancellableContinuation.resume(get())
                } catch (throwable: Throwable) {
                    val cause = throwable.cause ?: throwable
                    when (throwable) {
                        is CancellationException -> cancellableContinuation.cancel(cause)
                        else -> cancellableContinuation.resumeWithException(cause)
                    }
                }
            },
            Executor {
                it.run()
            },
        )

        cancellableContinuation.invokeOnCancellation {
            cancel(false)
        }
    }
}

// Get and set values in a worker
// ---------------------------------------------------------------------------------------------------------------------

internal fun ListenableWorker.getStringFromTag(property: String): String {
    return tags
        .first { it.startsWith(property) }
        .removePrefix(property)
}

internal fun ListenableWorker.getBooleanFromTag(property: String, defaultValue: Boolean): Boolean {
    return tags
        .first { it.startsWith(property) }
        .removePrefix(property)
        .toBooleanStrictOrNull()
        ?: defaultValue
}

internal fun ListenableWorker.getLongFromTag(property: String, defaultValue: Long): Long {
    return tags
        .first { it.startsWith(property) }
        .removePrefix(property)
        .toLongOrNull()
        ?: defaultValue
}

internal fun WorkInfo.getStringFromTag(property: String): String {
    return tags
        .first { it.startsWith(property) }
        .removePrefix(property)
}

internal fun WorkInfo.getBooleanFromTag(property: String, defaultValue: Boolean): Boolean {
    return tags
        .first { it.startsWith(property) }
        .removePrefix(property)
        .toBooleanStrictOrNull()
        ?: defaultValue
}

internal fun WorkInfo.getLongFromTag(property: String, defaultValue: Long): Long {
    return tags
        .first { it.startsWith(property) }
        .removePrefix(property)
        .toLongOrNull()
        ?: defaultValue
}
