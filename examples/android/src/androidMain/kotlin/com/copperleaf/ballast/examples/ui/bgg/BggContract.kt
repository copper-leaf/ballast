package com.copperleaf.ballast.examples.ui.bgg

import com.copperleaf.ballast.examples.api.models.BggHotListItem
import com.copperleaf.ballast.examples.api.models.HotListType
import com.copperleaf.ballast.repository.cache.Cached

object BggContract {
    data class State(
        val forceRefresh: Boolean = false,
        val bggHotListType: HotListType = HotListType.BoardGame,
        val bggHotList: Cached<List<BggHotListItem>> = Cached.NotLoaded(),
    )

    sealed class Inputs {
        object GoBack : Inputs()
        data class SetForceRefresh(val forceRefresh: Boolean) : Inputs()
        data class ChangeHotListType(val hotListType: HotListType) : Inputs()
        data class FetchHotList(val forceRefresh: Boolean) : Inputs()
        data class HotListUpdated(val bggHotList: Cached<List<BggHotListItem>>) : Inputs()
    }

    sealed class Events {
        object GoBack : Events()
    }
}
