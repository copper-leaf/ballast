package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

internal class RunningSideJob(
    internal val sideJobId: Int,
    internal val key: String,
    internal var restartState: SideJobScope.RestartState,
    internal var job: Job,
)

internal data class SideJobList(
    internal val key: String,
    internal val autoIncrement: Int = 1,
    internal val runningJobs: List<RunningSideJob> = emptyList(),
) {
    val currentSideJob: RunningSideJob? = runningJobs.lastOrNull()

    fun addNewRunningJob(
        parentJob: Job
    ): SideJobList {
        val restartState = if (autoIncrement > 1) {
            // if there are any active jobs at this key, we are restarting this sideJob
            SideJobScope.RestartState.Restarted
        } else {
            // if the list was empty or all previous sideJobs at this key have completed, then we are starting it afresh
            SideJobScope.RestartState.Initial
        }

        // make sure all runningJobs are cancelled, each key can only have 1 active sideJob at a time
        runningJobs.forEach { it.job.cancel() }

        val newSideJob = RunningSideJob(
            sideJobId = this.autoIncrement,
            key = this.key,
            restartState = restartState,
            job = SupervisorJob(parent = parentJob),
        )

        return copy(
            autoIncrement = this.autoIncrement + 1,
            runningJobs = this.runningJobs + newSideJob
        )
    }

    fun cancelSideJobs() {
        runningJobs.forEach { it.job.cancel() }
    }

    fun removeCompletedJob(id: Int): SideJobList {
        val sideJobAtKey = runningJobs.singleOrNull { it.sideJobId == id }

        if (sideJobAtKey != null) {
            // cancel the sideJob's coroutine, if needed
            sideJobAtKey.job.cancel()
            return copy(
                runningJobs = this.runningJobs - sideJobAtKey
            )
        } else {
            return copy()
        }

    }
}
