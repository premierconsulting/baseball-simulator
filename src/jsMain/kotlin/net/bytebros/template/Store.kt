package net.bytebros.template

import dev.fritz2.binding.RootStore
import kotlinx.coroutines.flow.map
import net.bytebros.baseball.model.BaseballState
import kotlin.math.ceil

enum class Hit {
    SINGLE,
    DOUBLE,
    TRIPLE,
    HOME_RUN
}

enum class Runner {
    FIRST,
    SECOND,
    THIRD
}

object BaseballStateStore: RootStore<BaseballState>(BaseballState()) {

    val handleStrike = handleAndEmit<Unit> { state ->
        val strikes = state.strikes + 1
        if (strikes > 2) emit(Unit)
        state.copy(
            strikes = strikes,
        )
    }

    val handleBatterOut = handleAndEmit<Unit> { state ->
        emit(Unit)
        state.copy(
            balls = 0,
            strikes = 0,
        )
    }

    val handleRunnerOut = handleAndEmit<Runner, Unit> { state, runner ->
        val base1 = if (runner == Runner.FIRST) false else state.base1
        val base2 = if (runner == Runner.SECOND) false else state.base2
        val base3 = if (runner == Runner.THIRD) false else state.base3
        emit(Unit)
        state.copy(
            base1 = base1,
            base2 = base2,
            base3 = base3,
        )
    }

    private val handleOut = handleAndEmit<Unit> { state ->
        val outs = state.outs + 1
        if (outs > 2) emit(Unit)
        state.copy(
            outs = outs,
        )
    }

    val handleInningChange = handleAndEmit<Unit> { state ->
        val gameOver = state.gameOverOnInningChange()
        if (gameOver) emit(Unit)
        state.copy(
            halfInning = if (gameOver) state.halfInning else state.halfInning + 1,
            balls = 0,
            strikes = 0,
            outs = 0,
            base1 = false,
            base2 = false,
            base3 = false
        )
    }

    val handleBall = handleAndEmit<Hit> { state ->
        val balls = state.balls + 1
        if (balls > 3) emit(Hit.SINGLE)
        state.copy(
            balls = balls,
        )
    }


    val handleHit = handleAndEmit<Hit, Int> { state, hit ->
        val hitIndex = hit.ordinal

        val runsScored = listOf(state.base3, state.base2, state.base1, true)
            .map { baseLoaded -> if (baseLoaded) 1 else 0 }
            .slice(0..hitIndex)
            .sum()

        if (runsScored > 0) {
            emit(runsScored)
        }

        val (base3, base2, base1) = listOf(state.base2, state.base1, true, false, false, false)
            .windowed(3)[hitIndex]

        state.copy(
            balls = 0,
            strikes = 0,
            base1 = base1,
            base2 = base2,
            base3 = base3,
        )
    }

    val handleScore = handleAndEmit<Int, Unit> { state, runsScored ->
        val awayScore = if (state.awayIsBatting) state.awayScore + runsScored else state.awayScore
        val homeScore = if (!state.awayIsBatting) state.homeScore + runsScored else state.homeScore

        // Home team can win on a hit, if they take the lead, and it's the 9th inning or higher
        if (state.homeIsBatting && homeScore > awayScore && state.halfInning > 16) emit(Unit)

        state.copy(
            awayScore = awayScore,
            homeScore = homeScore,
        )
    }

    val handleSteal = handleAndEmit<Runner, Int> { state, runner ->
        val stolen: Boolean = when(runner) {
            Runner.FIRST -> !state.base2
            Runner.SECOND -> !state.base3
            Runner.THIRD -> true
        }

        val (base3, base2, base1) = when(runner) {
            Runner.FIRST -> listOf(state.base3, true, false)
            Runner.SECOND -> listOf(true, false, state.base1)
            Runner.THIRD -> listOf(false, state.base2, state.base1)
        }

        if (stolen && runner == Runner.THIRD) {
            emit(1)
        }

        state.copy(
            base1 = if (stolen) base1 else state.base1,
            base2 = if (stolen) base2 else state.base2,
            base3 = if (stolen) base3 else state.base3
        )
    }

    private val handleWin = handleAndEmit<Unit> { state ->
        state.copy(
            final = true,
        )
    }

    init {
        handleStrike handledBy handleBatterOut
        handleBatterOut handledBy handleOut
        handleRunnerOut handledBy handleOut
        handleOut handledBy handleInningChange
        handleInningChange handledBy handleWin

        handleBall handledBy handleHit
        handleHit handledBy handleScore
        handleSteal handledBy handleScore
        handleScore handledBy handleWin
    }

    val awayScore = data.map { it.awayScore }
    val homeScore = data.map { it.homeScore }
    val topInning = data.map { it.topInning }
    val inning = data.map { it.inning }
    val base1 = data.map { it.base1 }
    val base2 = data.map { it.base2 }
    val base3 = data.map { it.base3 }
    val balls = data.map { it.balls }
    val strikes = data.map { it.strikes }
    val outs = data.map { it.outs }
    val final = data.map { it.final }
}