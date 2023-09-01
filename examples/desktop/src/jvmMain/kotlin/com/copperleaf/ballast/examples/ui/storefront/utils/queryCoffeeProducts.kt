package com.copperleaf.ballast.examples.ui.storefront.utils

import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProduct
import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProductColumn
import com.copperleaf.ballast.examples.ui.storefront.models.ColumnSort

internal fun CoffeeProduct.matchesSearchQuery(searchQuery: String): Boolean {
    return this.name.contains(searchQuery) ||
            this.description.contains(searchQuery)
}

internal fun CoffeeProduct.containsAllTags(tags: List<String>): Boolean {
    val productSortedTags = this.tags.sorted()
    val fitlerSortedTags = tags.sorted()
    return productSortedTags.containsAll(fitlerSortedTags)
}

internal fun CoffeeProduct.isInStock(): Boolean {
    return this.quantity > 0u
}

internal fun CoffeeProduct.priceIsInRange(priceRange: ClosedRange<UInt>): Boolean {
    return this.cost in priceRange
}

internal fun CoffeeProduct.ratingIsInRange(rating: UInt): Boolean {
    return this.rating > rating.toDouble()
}

internal fun List<ColumnSort<CoffeeProductColumn>>.asComparator(
    defaultSort: ColumnSort<CoffeeProductColumn> = ColumnSort(CoffeeProductColumn.Name, ColumnSort.Direction.Ascending)
): Comparator<CoffeeProduct> {
    val filterableColumns = this.filter { it.column.canSort }
    if(filterableColumns.isEmpty()) {
        return defaultSort.asComparator()
    }

    return filterableColumns
        .map { it.asComparator() }
        .reduce { acc, comparator -> acc.thenComparing(comparator) }
}

internal fun ColumnSort<CoffeeProductColumn>.asComparator() : Comparator<CoffeeProduct> {
    val propertySelector = { coffeeProduct: CoffeeProduct ->
        when(this.column) {
            CoffeeProductColumn.Name -> coffeeProduct.name
            CoffeeProductColumn.Description -> coffeeProduct.description
            CoffeeProductColumn.Tags -> error("cannot sort by tags")
            CoffeeProductColumn.Quantity -> coffeeProduct.quantity
            CoffeeProductColumn.Cost -> coffeeProduct.cost
            CoffeeProductColumn.Rating -> coffeeProduct.rating
            CoffeeProductColumn.NumberOfReviews -> coffeeProduct.numberOfReviews
        }
    }

    return when (sortDirection) {
        ColumnSort.Direction.Ascending -> compareBy(propertySelector)
        ColumnSort.Direction.Descending -> compareByDescending((propertySelector))
    }
}

internal fun <T, U> Sequence<T>.filterIfPresent(filterContext: U?, predicate: (U, T) -> Boolean): Sequence<T> {
    return if (filterContext != null) {
        filter { predicate(filterContext, it) }
    } else {
        this
    }
}

internal fun <T> Sequence<T>.filterIfTrue(shouldApplyFilter: Boolean, predicate: (T) -> Boolean): Sequence<T> {
    return if (shouldApplyFilter) {
        filter { predicate(it) }
    } else {
        this
    }
}
