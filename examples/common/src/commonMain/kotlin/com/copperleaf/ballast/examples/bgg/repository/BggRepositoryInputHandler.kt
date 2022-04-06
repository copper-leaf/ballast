package com.copperleaf.ballast.examples.bgg.repository

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.bgg.api.BggApi
import com.copperleaf.ballast.observeFlows
import com.copperleaf.ballast.postInput
import com.copperleaf.ballast.repository.bus.EventBus
import com.copperleaf.ballast.repository.bus.observeInputsFromBus
import com.copperleaf.ballast.repository.cache.fetchWithCache

class BggRepositoryInputHandler(
    private val eventBus: EventBus,
    private val api: BggApi,
) : InputHandler<
    BggRepositoryContract.Inputs,
    Any,
    BggRepositoryContract.State> {

    override suspend fun InputHandlerScope<
        BggRepositoryContract.Inputs,
        Any,
        BggRepositoryContract.State>.handleInput(
        input: BggRepositoryContract.Inputs
    ) = when (input) {
        is BggRepositoryContract.Inputs.ClearCaches -> {
            updateState { BggRepositoryContract.State() }
        }
        is BggRepositoryContract.Inputs.Initialize -> {
            val previousState = getCurrentState()

            if (!previousState.initialized) {
                updateState { it.copy(initialized = true) }
                // start observing flows here
                logger.debug("initializing")
                observeFlows(
                    key = "Observe account changes",
                    eventBus
                        .observeInputsFromBus<BggRepositoryContract.Inputs>(),
                )
            } else {
                logger.debug("already initialized")
                noOp()
            }
        }
        is BggRepositoryContract.Inputs.RefreshAllCaches -> {
            // then refresh all the caches in this repository
            val currentState = getCurrentState()
            if (currentState.bggHotListInitialized) {
                postInput(BggRepositoryContract.Inputs.RefreshBggHotList(currentState.bggHotListType, true))
            }

            Unit
        }

        is BggRepositoryContract.Inputs.BggHotListUpdated -> {
            updateState { it.copy(bggHotListType = input.hotListType, bggHotList = input.bggHotList) }
        }
        is BggRepositoryContract.Inputs.RefreshBggHotList -> {
            val previousState = getAndUpdateState { it.copy(bggHotListInitialized = true) }
            fetchWithCache(
                input = input,
                forceRefresh = input.forceRefresh || previousState.bggHotListType != input.hotListType,
                getValue = { it.bggHotList },
                updateState = { BggRepositoryContract.Inputs.BggHotListUpdated(input.hotListType, it) },
                doFetch = { api.getHotGames(input.hotListType) },
            )
        }
    }
}
