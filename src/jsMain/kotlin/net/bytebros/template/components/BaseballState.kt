package net.bytebros.template.components

import dev.fritz2.components.box
import dev.fritz2.components.flexBox
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.div
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun RenderContext.baseballState() {
    div({
        height { "100%" }
        background {
            color { primary.main }
        }
        color { primary.highlight }
    }) {
        div({
            display { grid }
            height { "100%" }
            rows { "3fr 1fr 2fr 3fr 1fr" }
            columns { "1fr" }
            direction { column }
            alignItems { center }
            paddings {
                vertical { normal }
                horizontal { small }
            }
        }) {
            score()
            inning()
            bases()
            count()
            hits()
        }
    }
}