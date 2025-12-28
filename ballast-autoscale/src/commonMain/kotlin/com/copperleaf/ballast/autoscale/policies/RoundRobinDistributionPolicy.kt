package com.copperleaf.ballast.autoscale.policies

import com.copperleaf.ballast.autoscale.DistributionPolicy

public class RoundRobinDistributionPolicy<Inputs : Any, Events : Any, State : Any> :
    DistributionPolicy<Inputs, Events, State> {

    override fun getPolicyState(): DistributionPolicy.PolicyState<Inputs, Events, State> {
        var currentIndex = -1
        return DistributionPolicy.PolicyState { pool ->
            currentIndex++

            if (currentIndex in pool.indices) {
                // incrementing the index stayed in bounds, so return the VM at that index
                pool.getOrNull(currentIndex)
            } else {
                // incrementing the index was no longer in bounds. Reset the index to 0 and return the first VM
                currentIndex = 0
                pool.getOrNull(currentIndex)
            }
        }
    }
}
