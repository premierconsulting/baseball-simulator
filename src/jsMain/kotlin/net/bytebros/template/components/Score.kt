package net.bytebros.template.components

import dev.fritz2.components.gridBox
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.div
import dev.fritz2.styling.h1
import dev.fritz2.styling.params.BoxParams
import dev.fritz2.styling.params.Style
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.bytebros.template.BaseballStateStore

@OptIn(ExperimentalCoroutinesApi::class)
fun RenderContext.score() {
    div({
        width { "100%" }
        display { grid }
        columns { "5fr 1fr 5fr" }
        css("place-items: center;")
        color { primary.highlight }
        fontSize { "2.25rem" }
    }) {
        div {
            +"AWAY"
        }
        div {}
        div {
            +"HOME"
        }
        scoreText {
            BaseballStateStore.awayScore.asText()
        }
        scoreText {
            +"-"
        }
        scoreText {
            BaseballStateStore.homeScore.asText()
        }
    }
}

fun RenderContext.scoreText(style: Style<BoxParams> = { }, content: Div.() -> Unit) {
    div({
        fontSize { "6rem" }
    }) {
        content()
    }
}