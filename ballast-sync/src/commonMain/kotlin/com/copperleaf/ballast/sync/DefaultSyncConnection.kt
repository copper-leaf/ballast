package com.copperleaf.ballast.sync

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.queuedInputs
import com.copperleaf.ballast.states
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
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
public class DefaultSyncConnection<Inputs : Any, Events : Any, State : Any>(
    private val clientType: ClientType,
    private val adapter: SyncConnectionAdapter<Inputs, Events, State>,
    private val bufferStates: (Flow<State>) -> Flow<State> = { it },
    private val bufferInputs: (Flow<Inputs>) -> Flow<Inputs> = { it },
) : SyncConnection<Inputs, Events, State> {

    override fun BallastInterceptorScope<Inputs, Events, State>.connectViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ): Unit = when (clientType) {
        ClientType.Source -> {
            connectSourceViewModel(notifications)
        }

        ClientType.Replica -> {
            connectReplicaViewModel(notifications)
        }

        ClientType.Spectator -> {
            connectSpectatorViewModel(notifications)
        }
    }

    // the Source ViewModel sends State changes to other ViewModels, and receives Inputs from other ViewModels
    private fun BallastInterceptorScope<Inputs, Events, State>.connectSourceViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
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
                        .states(bufferStates)
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
                .queuedInputs(bufferInputs)
                .collect { input ->
                    adapter.sendInputToSource(input)
                }
        }
    }

    // the Source ViewModel sends Inputs to the Source ViewModel, and receives States from the Source ViewModel
    @Suppress("UNUSED_PARAMETER")
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

    override fun toString(): String {
        return "DefaultSyncConnection(clientType=${clientType.name}, adapter=$adapter)"
    }

    /**
     * Defines the type of client connecting to the synchronization service. Typically, there is a single Source ViewModel,
     * and an unspecified number of Replicas or Spectators
     */
    public enum class ClientType {
        /**
         * This is the source-of-truth for all synchronized ViewModels. It sends its own State to all other ViewModels, and
         * processes the Inputs sent by them.
         */
        Source,

        /**
         * Replicas receive the State from the Source, and can also send Inputs back to the Source. If a replica ViewModel
         * processes an Input, it should not truly be considered handled until it has been sent to the Source and processed
         * by the Source. As a result, the Source VM will then be synchronzied back to the VM that sent the Input.
         */
        Replica,

        /**
         * A Spectator receives the State from the source, but cannot send Inputs to it. It's a read-only replica of the
         * Source ViewModel.
         */
        Spectator,
    }
}
