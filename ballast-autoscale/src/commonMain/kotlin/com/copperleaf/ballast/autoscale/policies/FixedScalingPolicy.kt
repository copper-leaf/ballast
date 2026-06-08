package com.copperleaf.ballast.autoscale.policies

import com.copperleaf.ballast.autoscale.ScalingPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class FixedScalingPolicy<Inputs : Any, Events : Any, State : Any>(
    private val replicas: Int,
) : ScalingPolicy<Inputs, Events, State> {

    override fun getReplicaCount(): Flow<Int> {
        return flowOf(replicas)
    }
}
