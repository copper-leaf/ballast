package com.copperleaf.ballast.examples.api

import com.copperleaf.ballast.examples.api.models.BggHotListItem
import com.copperleaf.ballast.examples.api.models.HotListType

interface BggApi {

    suspend fun getHotGames(type: HotListType) : List<BggHotListItem>
}

