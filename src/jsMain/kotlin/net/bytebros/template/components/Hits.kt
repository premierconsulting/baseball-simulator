package net.bytebros.template.components

import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.button
import dev.fritz2.styling.div
import net.bytebros.template.BaseballStateStore
import net.bytebros.template.Hit

fun RenderContext.hits() {
    val handleHit = BaseballStateStore.handleHit
    div({
        display { grid }
        columns {
            repeat(2) { "1fr" }
        }
        css("gap: 1rem 2rem;")
    }) {
        hitButton {
            clicks.map { Hit.SINGLE } handledBy handleHit
            +"Single"
        }
        hitButton {
            clicks.map { Hit.DOUBLE } handledBy handleHit
            +"Double"
        }
        hitButton {
            clicks.map { Hit.TRIPLE } handledBy handleHit
            +"Triple"
        }
        hitButton {
            clicks.map { Hit.HOME_RUN } handledBy handleHit
            +"Home Run"
        }
    }
}

fun RenderContext.hitButton(content: Button.() -> Unit) {
    button({
        background {
            color { primary.mainContrast }
        }
        color { primary.highlightContrast }
        fontSize { large }
        padding {
            smaller
        }
    }) {
        content()
    }
}