package com.copperleaf.ballast.examples.bgg.repository

import com.copperleaf.ballast.examples.bgg.models.BggHotListItem
import com.copperleaf.ballast.examples.bgg.models.HotListType
import com.copperleaf.ballast.repository.cache.Cached

object BggRepositoryContract {
    data class State(
        val initialized: Boolean = false,

        val bggHotListType: HotListType = HotListType.BoardGame,
        val bggHotListInitialized: Boolean = false,
        val bggHotList: Cached<List<BggHotListItem>> = Cached.NotLoaded(),
    )

    sealed class Inputs {
        object ClearCaches : Inputs()
        object Initialize : Inputs()
        object RefreshAllCaches : Inputs()

        data class BggHotListUpdated(val hotListType: HotListType, val bggHotList: Cached<List<BggHotListItem>>) : Inputs()
        data class RefreshBggHotList(val hotListType: HotListType, val forceRefresh: Boolean) : Inputs()
    }
}
