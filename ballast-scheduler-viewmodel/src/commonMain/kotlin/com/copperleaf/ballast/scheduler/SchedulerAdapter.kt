package com.copperleaf.ballast.scheduler

public fun interface SchedulerAdapter<Inputs : Any, Events : Any, State : Any> {
    public suspend fun SchedulerAdapterScope<Inputs, Events, State>.configureSchedules()
}
