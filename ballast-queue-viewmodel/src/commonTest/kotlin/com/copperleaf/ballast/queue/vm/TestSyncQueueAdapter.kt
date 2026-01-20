package com.copperleaf.ballast.queue.vm

import com.copperleaf.ballast.queue.QueueExecutor

class TestSyncQueueAdapter : QueueExecutor.Adapter<
        Unit,
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State,
        > {

    override fun getJobMetadata(payload: TestContract.Inputs) {
    }
}
