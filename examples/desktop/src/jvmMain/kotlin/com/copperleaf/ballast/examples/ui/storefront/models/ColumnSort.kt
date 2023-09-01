package com.copperleaf.ballast.examples.ui.storefront.models

public data class ColumnSort<T : Enum<T>>(
    val column: T,
    val sortDirection: Direction,
) {
    public enum class Direction {
        Ascending,
        Descending,
    }
}
