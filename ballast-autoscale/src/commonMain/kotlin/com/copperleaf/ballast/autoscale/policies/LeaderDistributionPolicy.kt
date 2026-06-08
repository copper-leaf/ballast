package com.copperleaf.ballast.autoscale.policies

import com.copperleaf.ballast.autoscale.DistributionPolicy

public class LeaderDistributionPolicy<Inputs : Any, Events : Any, State : Any> :
    DistributionPolicy<Inputs, Events, State> {

    override fun getPolicyState(): DistributionPolicy.PolicyState<Inputs, Events, State> {
        return DistributionPolicy.PolicyState { input, pool ->
            pool.firstOrNull()
        }
    }
}
