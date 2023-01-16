package com.copperleaf.ballast.examples.ui.bgg

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.repository.BggRepository
import com.copperleaf.ballast.observeFlows
import kotlinx.coroutines.flow.map

class BggInputHandler(
    val repository: BggRepository
) : InputHandler<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State> {
    override suspend fun InputHandlerScope<
        BggContract.Inputs,
        BggContract.Events,
        BggContract.State>.handleInput(
        input: BggContract.Inputs
    ) = when (input) {
        is BggContract.Inputs.ChangeHotListType -> {
            updateState { it.copy(bggHotListType = input.hotListType) }
        }
        is BggContract.Inputs.FetchHotList -> {
            val currentState = getCurrentState()
            observeFlows("FetchHotList") {
                listOf(
                    repository
                        .getBggHotList(currentState.bggHotListType, refreshCache = input.forceRefresh)
                        .map { BggContract.Inputs.HotListUpdated(it) }
                )
            }
        }
        is BggContract.Inputs.HotListUpdated -> {
            updateState { it.copy(bggHotList = input.bggHotList) }
        }
    }
}
