package com.copperleaf.ballast.scheduler.workmanager

import androidx.work.DirectExecutor
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import kotlin.coroutines.resumeWithException

internal suspend fun <T> ListenableFuture<T>.await(): T {
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
