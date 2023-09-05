package com.copperleaf.ballast.examples.ui.storefront.utils

import com.copperleaf.ballast.examples.ui.storefront.models.CoffeeProduct
import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.fakerConfig
import java.util.*
import kotlin.math.roundToInt

public fun generateCoffeeProducts(
    random: Random = Random(),
    faker: Faker = Faker(
        fakerConfig {
            this.random = random
        }
    ),
    numberOfProducts: Int = random.nextInt(50, 100),
): List<CoffeeProduct> {
    return List(numberOfProducts) {
        generateCoffeeProduct(random, faker)
    }
}

public fun generateCoffeeProduct(random: Random, faker: Faker): CoffeeProduct {
    return CoffeeProduct(
        name = faker.coffee.blendName(),
        brand = faker.coffee.country(),
        description = faker.coffee.notes(),
        tags = (listOf(faker.coffee.country()) + faker.coffee.notes().split(" ")).sorted(),

        quantity = if (random.nextInt(0, 100) <= 60) {
            // products have a 60% change of being in stock
            random.nextInt(1, 100).toUInt()
        } else {
            0u
        },
        cost = random.nextInt(1, 30).toUInt(),

        rating = random.nextDouble(0.0, 5.0).roundTo(),
        numberOfReviews = random.nextInt(0, 1000).toUInt(),
    )
}

fun Double.roundTo(): Double = this.times(10.0).roundToInt() / 10.0
