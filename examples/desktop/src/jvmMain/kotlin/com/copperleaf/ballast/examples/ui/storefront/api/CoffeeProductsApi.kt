package com.copperleaf.ballast.examples.ui.storefront.api

import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProduct
import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProductColumn
import com.copperleaf.ballast.examples.ui.storefront.models.ColumnSort

interface CoffeeProductsApi {

    public suspend fun getTotalProductsCount(): Int

    public suspend fun queryProducts(
        searchQuery: String?,
        filterByTags: List<String>?,
        filterInStock: Boolean,

        priceRange: ClosedRange<UInt>?,
        rating: UInt?,
        sortResultsBy: List<ColumnSort<CoffeeProductColumn>>,
    ): List<CoffeeProduct>

}
