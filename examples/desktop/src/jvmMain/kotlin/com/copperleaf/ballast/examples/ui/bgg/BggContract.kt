package com.copperleaf.ballast.examples.ui.bgg

import com.copperleaf.ballast.examples.api.models.BggHotListItem
import com.copperleaf.ballast.examples.api.models.HotListType
import com.copperleaf.ballast.repository.cache.Cached

object BggContract {
    data class State(
        val bggHotListType: HotListType = HotListType.BoardGame,
        val bggHotList: Cached<List<BggHotListItem>> = Cached.NotLoaded(),
    )

    sealed class Inputs {
        data class ChangeHotListType(val hotListType: HotListType) : com.copperleaf.ballast.examples.ui.bgg.BggContract.Inputs()
        data class FetchHotList(val forceRefresh: Boolean) : com.copperleaf.ballast.examples.ui.bgg.BggContract.Inputs()
        data class HotListUpdated(val bggHotList: Cached<List<BggHotListItem>>) : com.copperleaf.ballast.examples.ui.bgg.BggContract.Inputs()
    }

    sealed class Events {
    }
}
