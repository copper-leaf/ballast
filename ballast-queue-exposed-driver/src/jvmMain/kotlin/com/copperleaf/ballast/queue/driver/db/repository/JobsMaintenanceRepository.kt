package com.copperleaf.ballast.queue.driver.db.repository

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

public interface JobsMaintenanceRepository {
    public suspend fun deleteOldJobs(duration: Duration = 30.days)

    public suspend fun freeJobCooldowns()

    public suspend fun retryHungJobs()
}
