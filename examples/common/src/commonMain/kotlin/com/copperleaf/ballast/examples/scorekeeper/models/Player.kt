package com.copperleaf.ballast.examples.scorekeeper.models

import kotlin.math.abs

data class Player(
    val name: String,
    val score: Int = 0,
    val tempScore: Int = 0,
    val selected: Boolean = false,
) {
    val scoreDisplay: String = when {
        tempScore > 0 -> "$score + $tempScore"
        tempScore < 0 -> "$score - ${abs(tempScore)}"
        else -> "$score"
    }

    fun commitScore(): Player {
        return this.copy(
            score = this.score + this.tempScore,
            tempScore = 0,
            selected = false,
        )
    }
}
