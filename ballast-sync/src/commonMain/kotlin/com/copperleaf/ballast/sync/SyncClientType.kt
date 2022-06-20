package com.copperleaf.ballast.sync

/**
 * Defines the type of client connecting to the synchronization service. Typically, there is a single Source ViewModel,
 * and an unspecified number of Replicas or Spectators
 */
public enum class SyncClientType {
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
