package com.copperleaf.ballast.examples.repository

import com.copperleaf.ballast.examples.api.models.BggHotListItem
import com.copperleaf.ballast.examples.api.models.HotListType
import com.copperleaf.ballast.repository.cache.Cached
import kotlinx.coroutines.flow.Flow

interface BggRepository {

    fun clearAllCaches()
    fun getBggHotList(hotListType: HotListType, refreshCache: Boolean = false): Flow<Cached<List<BggHotListItem>>>

}
