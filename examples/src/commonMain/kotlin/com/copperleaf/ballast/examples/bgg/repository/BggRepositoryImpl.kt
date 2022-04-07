package com.copperleaf.ballast.examples.bgg.repository

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.examples.bgg.api.BggApi
import com.copperleaf.ballast.examples.bgg.models.BggHotListItem
import com.copperleaf.ballast.examples.bgg.models.HotListType
import com.copperleaf.ballast.repository.BallastRepository
import com.copperleaf.ballast.repository.bus.EventBus
import com.copperleaf.ballast.repository.cache.Cached
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BggRepositoryImpl(
    coroutineScope: CoroutineScope,
    eventBus: EventBus,
    configBuilder: BallastViewModelConfiguration.Builder,
    api: BggApi,
) : BallastRepository<BggRepositoryContract.Inputs, BggRepositoryContract.State>(
    coroutineScope = coroutineScope,
    eventBus = eventBus,
    configBuilder = configBuilder
        .apply {
            this.inputHandler = BggRepositoryInputHandler(eventBus, api)
            this.initialState = BggRepositoryContract.State()
            this.name = "Bgg Repository"
        },
), BggRepository {
    override fun clearAllCaches() {
        trySend(BggRepositoryContract.Inputs.ClearCaches)
    }

    override fun getBggHotList(hotListType: HotListType, refreshCache: Boolean): Flow<Cached<List<BggHotListItem>>> {
        trySend(BggRepositoryContract.Inputs.Initialize)
        trySend(BggRepositoryContract.Inputs.RefreshBggHotList(hotListType, refreshCache))
        return observeStates()
            .map { it.bggHotList }
    }
}
