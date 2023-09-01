package com.copperleaf.ballast.examples.ui.storefront.api

import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProduct
import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProductColumn
import com.copperleaf.ballast.examples.ui.storefront.models.ColumnSort
import com.copperleaf.ballast.examples.ui.storefront.utils.asComparator
import com.copperleaf.ballast.examples.ui.storefront.utils.containsAllTags
import com.copperleaf.ballast.examples.ui.storefront.utils.filterIfPresent
import com.copperleaf.ballast.examples.ui.storefront.utils.filterIfTrue
import com.copperleaf.ballast.examples.ui.storefront.utils.generateCoffeeProducts
import com.copperleaf.ballast.examples.ui.storefront.utils.isInStock
import com.copperleaf.ballast.examples.ui.storefront.utils.matchesSearchQuery
import com.copperleaf.ballast.examples.ui.storefront.utils.priceIsInRange
import com.copperleaf.ballast.examples.ui.storefront.utils.ratingIsInRange
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

public class CoffeeProductsApiImpl(
    private val mockCoffeeProducts: List<CoffeeProduct> = generateCoffeeProducts()
) : CoffeeProductsApi {

    override suspend fun getTotalProductsCount(): Int {
        return mockCoffeeProducts.size
    }

    override suspend fun queryProducts(
        searchQuery: String?,
        filterByTags: List<String>?,
        filterInStock: Boolean,

        priceRange: ClosedRange<UInt>?,
        rating: UInt?,
        sortResultsBy: List<ColumnSort<CoffeeProductColumn>>,
    ): List<CoffeeProduct> {
        delay(1.5.seconds)
        return mockCoffeeProducts
            .asSequence()
            .filterIfPresent(searchQuery) { _searchQuery, product -> product.matchesSearchQuery(_searchQuery) }
            .filterIfPresent(filterByTags) { _filterByTags, product -> product.containsAllTags(_filterByTags) }
            .filterIfTrue(filterInStock) { product -> product.isInStock() }
            .filterIfPresent(priceRange) { _priceRange, product -> product.priceIsInRange(_priceRange) }
            .filterIfPresent(rating) { _rating, product -> product.ratingIsInRange(_rating) }
            .sortedWith(sortResultsBy.asComparator())
            .toList()
    }

}
