package com.copperleaf.ballast.examples.repository

import com.copperleaf.ballast.examples.api.models.BggHotListItem
import com.copperleaf.ballast.examples.api.models.HotListType
import com.copperleaf.ballast.repository.cache.Cached

object BggRepositoryContract {
    data class State(
        val initialized: Boolean = false,

        val bggHotListType: HotListType = HotListType.BoardGame,
        val bggHotListInitialized: Boolean = false,
        val bggHotList: Cached<List<BggHotListItem>> = Cached.NotLoaded(),
    )

    sealed interface Inputs {
        data object ClearCaches : Inputs
        data object Initialize : Inputs
        data object RefreshAllCaches : Inputs

        data class BggHotListUpdated(val hotListType: HotListType, val bggHotList: Cached<List<BggHotListItem>>) : Inputs
        data class RefreshBggHotList(val hotListType: HotListType, val forceRefresh: Boolean) : Inputs
    }
}
