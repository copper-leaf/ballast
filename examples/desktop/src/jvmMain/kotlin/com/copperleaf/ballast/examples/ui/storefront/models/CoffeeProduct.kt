package com.copperleaf.ballast.examples.ui.storefront.models

public data class CoffeeProduct(
    val name: String,
    val brand: String,
    val description: String,
    val tags: List<String>,

    val quantity: UInt,
    val cost: UInt,

    val rating: Double,
    val numberOfReviews: UInt,
)
