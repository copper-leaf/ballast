package com.copperleaf.ballast.queue.throttle

import com.copperleaf.ballast.queue.QueueThrottle
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.sync.Semaphore

/**
 * A [QueueThrottle] implementation that limits the total number of active jobs across all queues to
 * [maxConcurrentJobs]. This allows you to safely run multiple redundant workers for each queue, but limiting the
 * overall concurrency of the whole system to avoid overwhelming your process.
 *
 * As an example, you may have a system with three queues: "high", "default", and "low" priority. Each queue has 4
 * workers running in parallel, but we want to limit the total system load to 4 jobs at a time. Thus, you could end up
 * with a scenario where all 4 "high" priority jobs are running, and the "default" and "low" priority queues are
 * waiting for permits to become available. Or alternatively, 2 "high", 1 "default", and 1 "low", etc.
 *
 * In general, the max concurrency should at least the max number of workers for any given queue, to ensure all workers
 * are actually able to be utilized if needed. If the concurrency limit is lower than the number of workers for a given
 * queue, at least 1 worker will always be idle, and thus simply wasting system resources.
 */
public class ConcurrencyLimitThrottle(
    private val maxConcurrentJobs: Int
) : QueueThrottle {

    private val semaphore = Semaphore(maxConcurrentJobs)

    @Volatile
    private var shuttingDown = false

    override suspend fun acquirePermit(queueName: String): QueueThrottle.Permit {
        if (shuttingDown) awaitCancellation()

        semaphore.acquire()

        // Double-check after potentially blocking on the semaphore.
        if (shuttingDown) {
            semaphore.release()
            awaitCancellation()
        }

        return QueueThrottle.Permit {
            semaphore.release()
        }
    }

    override suspend fun awaitShutdown() {
        shuttingDown = true
        // Acquire every permit in the semaphore. The idle slots are grabbed immediately; slots held by active
        // workers become available one-by-one as each job completes. Once we hold all maxConcurrentJobs permits
        // we know every in-flight job has finished.
        repeat(maxConcurrentJobs) { semaphore.acquire() }
    }
}
