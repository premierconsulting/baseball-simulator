package net.bytebros.baseball.model

import dev.fritz2.lenses.Lenses
import kotlinx.serialization.Serializable
import kotlin.math.floor

@Lenses
@Serializable
data class Team(
    val name: String,
)

@Lenses
@Serializable
data class Score(
    val awayScore: Int = 0,
    val homeScore: Int = 0,
)

@Lenses
@Serializable
data class Bases(
    val base1: Boolean = false,
    val base2: Boolean = false,
    val base3: Boolean = false,
)

@Lenses
@Serializable
data class Count(
    val balls: Int = 0,
    val strikes: Int = 0,
    val out: Int = 0,
)

@Lenses
@Serializable
data class BaseballState(
    val awayScore: Int = 0,
    val homeScore: Int = 0,
    val halfInning: Int = 0,
    val base1: Boolean = false,
    val base2: Boolean = false,
    val base3: Boolean = false,
    val balls: Int = 0,
    val strikes: Int = 0,
    val outs: Int = 0,
    val final: Boolean = false,
) {
    val awayIsBatting: Boolean
        get() = halfInning % 2 == 0
    val homeIsBatting: Boolean
        get() = !awayIsBatting

    val topInning: Boolean = halfInning % 2 == 0
    val inning: Int = floor(halfInning / 2.0).toInt() + 1

    fun gameOverOnInningChange(): Boolean {
        if (halfInning > BaseballState.TOP_OF_THE_9TH && awayScore > homeScore && !awayIsBatting) {
            return true
        }
        if (halfInning >= BaseballState.TOP_OF_THE_9TH && homeScore > awayScore) {
            return true
        }
        return false
    }

    companion object {
        const val TOP_OF_THE_9TH = 16
    }
}

@Lenses
@Serializable
data class BaseballStateEvent(val data: BaseballState)