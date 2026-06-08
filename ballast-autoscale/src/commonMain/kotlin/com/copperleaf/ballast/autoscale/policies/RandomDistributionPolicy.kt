package com.copperleaf.ballast.autoscale.policies

import com.copperleaf.ballast.autoscale.DistributionPolicy
import kotlin.random.Random

public class RandomDistributionPolicy<Inputs : Any, Events : Any, State : Any>(
    private val random: Random = Random.Default,
) : DistributionPolicy<Inputs, Events, State> {

    override fun getPolicyState(): DistributionPolicy.PolicyState<Inputs, Events, State> {
        return DistributionPolicy.PolicyState { input, pool ->
            pool.randomOrNull(random)
        }
    }
}
