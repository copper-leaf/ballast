package com.copperleaf.ballast.sync

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A [SyncConnectionAdapter] that shares States/Inputs values directly in memory with no serialization required.
 */
public class InMemorySyncAdapter<
    Inputs : Any,
    Events : Any,
    State : Any>(
) : SyncConnectionAdapter<Inputs, Events, State> {
    private val synchronizedState = MutableStateFlow<State?>(null)
    private val synchronizedInputs = Channel<Inputs>(capacity = UNLIMITED)

    override suspend fun sendSynchronizedStateToReplicas(state: State) {
        synchronizedState.value = state
    }

    override suspend fun sendInputToSource(input: Inputs) {
        synchronizedInputs.send(input)
    }

    override suspend fun readStateFromSource(): Flow<State> {
        return synchronizedState.asStateFlow().filterNotNull()
    }

    override suspend fun readInputsFromReplicas(): Flow<Inputs> {
        return synchronizedInputs.receiveAsFlow()
    }
}
