package com.copperleaf.ballast.sync

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * The default connection implementation. This connection requires a single "Source" ViewModel, and new ViewModels that
 * start up as "Source" will cancel the previous one and take over that responsibility. It may have an unlimited number
 * of replicas or spectators.
 *
 * This connection uses a pluggable adapter, which handle the actual responsibility of dispatching States/Inputs to
 * the other ViewModels. You can use [InMemorySyncAdapter] to share those values directly in memory with no
 * serialization required, or you can create a custom adapter to serialize these values and share in some other way,
 * such as over a network.
 */
public class DefaultSyncConnection<
    Inputs : Any,
    Events : Any,
    State : Any>(
    private val adapter: SyncConnectionAdapter<Inputs, Events, State>,
) : SyncConnection<Inputs, Events, State> {

    private var sourceJob: Job? = null

    override fun BallastInterceptorScope<Inputs, Events, State>.connectViewModel(
        clientType: SyncClientType,
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ): Unit = when (clientType) {
        SyncClientType.Source -> {
            connectSourceViewModel(notifications)
        }
        SyncClientType.Replica -> {
            connectReplicaViewModel(notifications)
        }
        SyncClientType.Spectator -> {
            connectSpectatorViewModel(notifications)
        }
    }

    // the Source ViewModel sends State changes to other ViewModels, and receives Inputs from other ViewModels
    private fun BallastInterceptorScope<Inputs, Events, State>.connectSourceViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        sourceJob?.cancel()
        sourceJob = launch(start = CoroutineStart.UNDISPATCHED) {
            joinAll(
                launch(start = CoroutineStart.UNDISPATCHED) {
                    adapter
                        .readInputsFromReplicas()
                        .collect { input ->
                            this@connectSourceViewModel.sendToQueue(
                                Queued.HandleInput(null, input)
                            )
                        }
                },
                launch(start = CoroutineStart.UNDISPATCHED) {
                    notifications
                        .filterIsInstance<BallastNotification.StateChanged<Inputs, Events, State>>()
                        .map { it.state }
                        .onEach { delay(500) }
                        .collect { state ->
                            adapter.sendSynchronizedStateToReplicas(state)
                        }
                }
            )
        }
    }

    // the Source ViewModel sends Inputs to the Source ViewModel, and receives States from the Source ViewModel
    private fun BallastInterceptorScope<Inputs, Events, State>.connectReplicaViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            adapter
                .readStateFromSource()
                .collect { state ->
                    this@connectReplicaViewModel.sendToQueue(
                        Queued.RestoreState(null, state)
                    )
                }
        }
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .filterIsInstance<BallastNotification.InputQueued<Inputs, Events, State>>()
                .map { it.input }
                .onEach { delay(250) }
                .collect { input ->
                    adapter.sendInputToSource(input)
                }
        }
    }

    // the Source ViewModel sends Inputs to the Source ViewModel, and receives States from the Source ViewModel
    private fun BallastInterceptorScope<Inputs, Events, State>.connectSpectatorViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            adapter
                .readStateFromSource()
                .collect { state ->
                    this@connectSpectatorViewModel.sendToQueue(
                        Queued.RestoreState(null, state)
                    )
                }
        }
    }

}
