package com.copperleaf.ballast.examples.bgg.api

import com.copperleaf.ballast.examples.bgg.models.BggHotListItem
import com.copperleaf.ballast.examples.bgg.models.HotListType

interface BggApi {

    suspend fun getHotGames(type: HotListType) : List<BggHotListItem>
}
