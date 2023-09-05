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

    sealed interface Inputs {
        data object GoBack : Inputs
        data class SetForceRefresh(val forceRefresh: Boolean) : Inputs
        data class ChangeHotListType(val hotListType: HotListType) : Inputs
        data class FetchHotList(val forceRefresh: Boolean) : Inputs
        data class HotListUpdated(val bggHotList: Cached<List<BggHotListItem>>) : Inputs
    }

    sealed interface Events {
        data object GoBack : Events
    }
}
