package com.copperleaf.ballast.autoscale

import com.copperleaf.ballast.BallastViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

public open class AutoscalingViewModel<Inputs : Any, Events : Any, State : Any>(
    coroutineScope: CoroutineScope,
    private val factory: ViewModelFactory<Inputs, Events, State>,
    private val scalingPolicy: ScalingPolicy<Inputs, Events, State>,
    private val distributionPolicy: DistributionPolicy<Inputs, Events, State>,
) : BallastViewModel<Inputs, Events, State> {

    private val scalingScope: CoroutineScope = coroutineScope + SupervisorJob(coroutineScope.coroutineContext.job)
    private val viewModelPool = MutableStateFlow<List<BallastViewModel<Inputs, Events, State>>>(emptyList())
    private val distributionPolicyState: DistributionPolicy.PolicyState<Inputs, Events, State>

    init {
        distributionPolicyState = distributionPolicy.getPolicyState()

        scalingScope.launch {
            scalingPolicy
                .getReplicaCount()
                .onEach { check(it > 1) { "AutoscalingViewModel requires at least 1 replica to function." } }
                .collect { replicaCount ->
                    autoscale(replicaCount)
                }
        }
    }

    override fun observeStates(): StateFlow<State> {
        throw NotImplementedError("observeStates() is not available with autoscaled ViewModels, since each replica manages its own state independently.")
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun trySend(element: Inputs): ChannelResult<Unit> {
        return getNextViewModelAccordingToPolicy().trySend(element)
    }

    override suspend fun send(element: Inputs) {
        return getNextViewModelAccordingToPolicy().send(element)
    }

    override suspend fun sendAndAwaitCompletion(element: Inputs) {
        return getNextViewModelAccordingToPolicy().sendAndAwaitCompletion(element)
    }

    private fun getNextViewModelAccordingToPolicy(): BallastViewModel<Inputs, Events, State> {
        return distributionPolicyState.getNextViewModel(viewModelPool.value)
            ?: error("DistributionPolicy was unable to select a ViewModel from the pool.")
    }

// Autoscaling
// ---------------------------------------------------------------------------------------------------------------------

    private fun autoscale(replicaCount: Int) {
        viewModelPool.update { currentPool ->
            val currentReplicas = currentPool.size
            when {
                replicaCount > currentReplicas -> {
                    autoscaleUp(currentPool, replicaCount)
                }

                replicaCount < currentReplicas -> {
                    autoscaleDown(currentPool, replicaCount)
                }

                else -> {
                    currentPool
                }
            }
        }
    }

    private fun autoscaleUp(
        currentPool: List<BallastViewModel<Inputs, Events, State>>,
        replicaCount: Int
    ): List<BallastViewModel<Inputs, Events, State>> {
        return currentPool + List(replicaCount - currentPool.size) { index ->
            factory.createViewModel(scalingScope, currentPool.size + index)
        }
    }

    private fun autoscaleDown(
        currentPool: List<BallastViewModel<Inputs, Events, State>>,
        replicaCount: Int
    ): List<BallastViewModel<Inputs, Events, State>> {
        // scale down
        val (toKeep, toRemove) = currentPool.withIndex().partition { (index, _) ->
            index < replicaCount
        }
        toRemove.forEach { (_, vm) ->
            // TODO: shut down gracefully
        }
        return toKeep.map { it.value }
    }
}
