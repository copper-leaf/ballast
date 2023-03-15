package com.copperleaf.ballast.debugger.idea.repository

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class RepositoryEventHandler : EventHandler<
        RepositoryContract.Inputs,
        RepositoryContract.Events,
        RepositoryContract.State> {
    override suspend fun EventHandlerScope<
            RepositoryContract.Inputs,
            RepositoryContract.Events,
            RepositoryContract.State>.handleEvent(
        event: RepositoryContract.Events
    ) = when (event) {
        else -> {}
    }
}
