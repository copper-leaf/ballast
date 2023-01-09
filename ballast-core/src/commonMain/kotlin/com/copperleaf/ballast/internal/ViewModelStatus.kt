package com.copperleaf.ballast.internal

internal sealed interface Status {
    fun checkCanStart()
    fun checkCanShutDown()
    fun checkCanClear()

    fun checkStateChangeOpen()
    fun checkMainQueueOpen()
    fun checkEventsOpen()
    fun checkSideJobsOpen()
    fun checkSideJobCancellationOpen()

    object NotStarted : Status {
        override fun checkCanStart() {}

        override fun checkCanShutDown() {
            error("VM is not started!")
        }

        override fun checkCanClear() {

        }

        override fun checkStateChangeOpen() {
            error("VM is not started!")
        }

        override fun checkMainQueueOpen() {
            error("VM is not started!")
        }

        override fun checkEventsOpen() {
            error("VM is not started!")
        }

        override fun checkSideJobsOpen() {
            error("VM is not started!")
        }

        override fun checkSideJobCancellationOpen() {
            error("VM is not started!")
        }
    }

    object Running : Status {
        override fun checkCanStart() {
            error("VM is already started")
        }

        override fun checkCanShutDown() {}

        override fun checkCanClear() {

        }

        override fun checkStateChangeOpen() {}
        override fun checkMainQueueOpen() {}
        override fun checkEventsOpen() {}
        override fun checkSideJobsOpen() {}
        override fun checkSideJobCancellationOpen() {}
    }

    data class ShuttingDown(
        val stateChangeOpen: Boolean,
        val mainQueueOpen: Boolean,
        val eventsOpen: Boolean,
        val sideJobsOpen: Boolean,
        val sideJobsCancellationOpen: Boolean,
    ) : Status {
        override fun checkCanStart() {
            error("VM is already started and is shutting down, it cannot be restarted")
        }

        override fun checkCanShutDown() {
            error("VM is already shutting down!")
        }

        override fun checkCanClear() {

        }

        override fun checkStateChangeOpen() {
            if (!stateChangeOpen) error("VM is shutting down and the state can no longer be changed")
        }


        override fun checkMainQueueOpen() {
            if (!mainQueueOpen) error("VM is shutting down and no more Inputs can be accepted!")
        }

        override fun checkEventsOpen() {
            if (!eventsOpen) error("VM is shutting down and no more Events can be accepted!")
        }

        override fun checkSideJobsOpen() {
            if (!sideJobsOpen) error("VM is shutting down and no more SideJobs can be started!")
        }

        override fun checkSideJobCancellationOpen() {
            if (!sideJobsCancellationOpen) error("VM is shutting down and SideJobs can no longer be manually cancelled!")
        }
    }

    object Cleared : Status {
        override fun checkCanStart() {
            error("VM is cleared, it cannot be restarted")
        }

        override fun checkCanShutDown() {
            error("VM is cleared!")
        }

        override fun checkCanClear() {
            error("VM is already cleared!")
        }

        override fun checkStateChangeOpen() {
            error("VM is cleared!")
        }

        override fun checkMainQueueOpen() {
            error("VM is cleared!")
        }

        override fun checkEventsOpen() {
            error("VM is cleared!")
        }

        override fun checkSideJobsOpen() {
            error("VM is cleared!")
        }

        override fun checkSideJobCancellationOpen() {
            error("VM is cleared!")
        }
    }
}
