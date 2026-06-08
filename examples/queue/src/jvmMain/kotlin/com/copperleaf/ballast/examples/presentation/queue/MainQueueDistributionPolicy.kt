package com.copperleaf.ballast.examples.presentation.queue

import com.copperleaf.ballast.autoscale.DistributionPolicy

public class MainQueueDistributionPolicy : DistributionPolicy<
        MainQueueContract.Inputs,
        MainQueueContract.Events,
        MainQueueContract.State> {

    override fun getPolicyState(): DistributionPolicy.PolicyState<
            MainQueueContract.Inputs,
            MainQueueContract.Events,
            MainQueueContract.State> {
        return DistributionPolicy.PolicyState { input, pool ->
            when (input) {
                is MainQueueContract.Inputs.MainJob -> {
                    pool.getOrNull(input.queue.ordinal)
                }
            }
        }
    }
}
