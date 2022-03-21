package com.copperleaf.ballast.examples.bgg.repository

import com.copperleaf.ballast.examples.bgg.models.BggHotListItem
import com.copperleaf.ballast.examples.bgg.models.HotListType
import com.copperleaf.ballast.repository.cache.Cached
import kotlinx.coroutines.flow.Flow

interface BggRepository {

    fun clearAllCaches()
    fun getBggHotList(hotListType: HotListType, refreshCache: Boolean = false): Flow<Cached<List<BggHotListItem>>>

}
