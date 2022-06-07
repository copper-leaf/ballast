package com.copperleaf.ballast.examples.bgg.ui

import com.copperleaf.ballast.examples.bgg.models.BggHotListItem
import com.copperleaf.ballast.examples.bgg.models.HotListType
import com.copperleaf.ballast.repository.cache.Cached

object BggContract {
    data class State(
        val bggHotListType: HotListType = HotListType.BoardGame,
        val bggHotList: Cached<List<BggHotListItem>> = Cached.NotLoaded(),
    )

    sealed class Inputs {
        data class ChangeHotListType(val hotListType: HotListType) : Inputs()
        data class FetchHotList(val forceRefresh: Boolean) : Inputs()
        data class HotListUpdated(val bggHotList: Cached<List<BggHotListItem>>) : Inputs()
        object GoBack : Inputs()
    }

    sealed class Events {
        object NavigateBackwards : Events()
    }
}
