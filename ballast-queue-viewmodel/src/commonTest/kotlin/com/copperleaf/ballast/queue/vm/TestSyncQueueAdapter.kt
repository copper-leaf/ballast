package com.copperleaf.ballast.queue.vm

import com.copperleaf.ballast.queue.QueueDriver

class TestSyncQueueAdapter : QueueDriver.Adapter<
        Unit,
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State,
        > {

    override fun getJobMetadata(payload: TestContract.Inputs) {
    }
}
