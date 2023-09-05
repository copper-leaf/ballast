package com.copperleaf.ballast.examples.ui.storefront

import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProduct
import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProductColumn
import com.copperleaf.ballast.examples.ui.storefront.models.ColumnSort

public object StorefrontContract {
    public data class State(
        val loading: Boolean = false,

        val filteredCoffeeProducts: List<CoffeeProduct> = emptyList(),
        val totalNumberOfProducts: Int = 0,

        val searchQuery: String = "",
        val filterByTags: List<String> = emptyList(),
        val filterInStock: Boolean = false,
        val priceRange: ClosedRange<UInt> = 0u..UInt.MAX_VALUE,
        val filterByRating: UInt? = null,
        val sortResultsBy: List<ColumnSort<CoffeeProductColumn>> = emptyList(),
    ) {
        override fun toString(): String {
            return "State(searchQuery='$searchQuery', filterByTags=$filterByTags, filterInStock=$filterInStock, priceRange=$priceRange, filterByRating=$filterByRating, sortResultsBy=$sortResultsBy)"
        }
    }

    public sealed class Inputs {
        data object Initialize : Inputs()

        data class UpdateSearchQuery(val searchQuery: String): Inputs()

        data class ToggleColumnSort(val column: CoffeeProductColumn): Inputs()
        data class ToggleTag(val tag: String): Inputs()
        data object ToggleFilterInStock: Inputs()

        data class UpdatePriceRangeMin(val minPrice: UInt): Inputs()
        data class UpdatePriceRangeMax(val maxPrice: UInt): Inputs()
        data class UpdateRating(val rating: UInt): Inputs()

        data object QueryCoffeeProducts: Inputs()
    }

    public sealed class Events {
    }
}
