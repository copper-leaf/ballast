package com.copperleaf.ballast.examples.presentation.utils

import kotlin.random.Random

fun randomString(length: Int = 10, random: Random): String {
    val allowedChars = (('A'..'Z') + ('a'..'z') + ('0'..'9'))
    return (1..length)
        .map { allowedChars.random(random) }
        .joinToString("")
}
