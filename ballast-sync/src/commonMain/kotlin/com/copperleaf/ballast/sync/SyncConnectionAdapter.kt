package com.copperleaf.ballast.sync

import kotlinx.coroutines.flow.Flow

/**
 * A generic interface for sending and receiving synchronized values. Adapters are typically used by
 * [DefaultSyncConnection], and any custom [SyncConnection] should use the adapters rather than manually serializing
 * States/Inputs to maximize flexibility.
 */
public interface SyncConnectionAdapter<
    Inputs : Any,
    Events : Any,
    State : Any> {

    public suspend fun sendSynchronizedStateToReplicas(state: State)
    public suspend fun sendInputToSource(input: Inputs)

    public suspend fun readStateFromSource(): Flow<State>
    public suspend fun readInputsFromReplicas(): Flow<Inputs>
}
