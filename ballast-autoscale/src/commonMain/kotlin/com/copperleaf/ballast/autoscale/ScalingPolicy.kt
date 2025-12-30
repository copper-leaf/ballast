package com.copperleaf.ballast.autoscale

import kotlinx.coroutines.flow.Flow

public fun interface ScalingPolicy<Inputs : Any, Events : Any, State : Any> {
    public fun getReplicaCount(): Flow<Int>
}
