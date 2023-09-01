package com.copperleaf.ballast.examples.ui.storefront.models

public enum class CoffeeProductColumn(val canSort: Boolean) {
    Name(canSort = true),
    Description(canSort = true),
    Tags(canSort = false),
    Quantity(canSort = true),
    Cost(canSort = true),
    Rating(canSort = true),
    NumberOfReviews(canSort = true),
}
