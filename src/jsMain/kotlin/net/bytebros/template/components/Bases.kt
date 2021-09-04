package net.bytebros.template.components

import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.div
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.bytebros.template.BaseballStateStore
import net.bytebros.template.Runner

fun RenderContext.bases() {
    div({
        display { grid }
        columns { "3rem 3rem" }
        gap { "10px" }
        css("transform: rotate(45deg)")
        margins {
            horizontal { auto }
        }
    }) {
        base(BaseballStateStore.base1, Runner.FIRST)
        base(BaseballStateStore.base2, Runner.SECOND)
        base(BaseballStateStore.base3, Runner.THIRD)
    }
}

fun RenderContext.base(onBase: Flow<Boolean>, runner: Runner) {
    val classes = onBase.map { on ->
        mapOf("on" to on)
    }
    div({
        height {
            "3rem"
        }
        width {
            "3rem"
        }
        border {
            width { normal }
            style { solid }
            color { primary.highlight }
        }
        children("&.on") {
            background {
                color { primary.highlight }
            }
        }
        children("&:nth-of-type(2)") {
            css("order: -1")
        }
    }) {
        classMap(classes)
        clicks.map { runner } handledBy BaseballStateStore.handleRunnerOut
        contextmenus.preventDefault().map { runner } handledBy BaseballStateStore.handleSteal
    }
}