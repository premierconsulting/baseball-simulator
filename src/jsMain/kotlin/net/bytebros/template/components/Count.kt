package net.bytebros.template.components

import dev.fritz2.binding.Handler
import dev.fritz2.components.box
import dev.fritz2.components.flexBox
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.div
import dev.fritz2.styling.params.DisplayValues.flex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bytebros.template.BaseballStateStore

fun RenderContext.countCircle(countFlow: Flow<Int>, index: Int, color: String) {
    box({
        width { small }
        height { small }
        border {
            width { "2px" }
            style { solid }
            color { primary.highlight }
        }
        padding { small }
        background {
            color { primary.main }
        }
        children("&.highlighted") {
            background {
                color { primary.highlight }
            }
        }
        radius { "50%" }
    }) {
        val classes = countFlow.map { count ->
            val highlighted = count >= index
            mapOf(
                "highlighted" to highlighted
            )
        }
        classMap(
            classes
        )
    }
}

data class CountSectionProps(
    val label: String,
    val countFlow: Flow<Int>,
    val onClick: Handler<Unit>,
    val max: Int = 2,
    val color: String = "red",
)

fun RenderContext.countSection(props: CountSectionProps) {
    val (
        label,
        countFlow,
        onClick,
        max,
        color
    ) = props
    div({
        display { grid }
        columns {
            repeat(4) { "1fr" }
        }
        css("place-items: center;")
        gap { "1rem" }
    }) {
        clicks handledBy onClick

        div({
            fontSize { huge }
        }){
            +label
        }
        for(index in 1..max) {
            countCircle(countFlow, index, color)
        }
        for(index in max..3) {
            div {  }
        }
    }
}

fun RenderContext.count() {
    val propsList = listOf(
        CountSectionProps("B", BaseballStateStore.balls, BaseballStateStore.handleBall, 3, "green"),
        CountSectionProps("S", BaseballStateStore.strikes, BaseballStateStore.handleStrike),
        CountSectionProps("O", BaseballStateStore.outs, BaseballStateStore.handleBatterOut),
    )

    div({
        margin { auto }
    }) {
        propsList.forEach { props ->
            countSection(props)
        }
    }
}