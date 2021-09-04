package net.bytebros.template.components

import dev.fritz2.components.box
import dev.fritz2.components.icon
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.div
import dev.fritz2.styling.span
import kotlinx.coroutines.flow.map
import net.bytebros.template.BaseballStateStore
import kotlin.math.floor
import kotlin.math.sqrt

fun RenderContext.inning() {
    div({
        display { flex }
        justifyContent { center }
        alignItems { center }
        fontSize { "4rem" }
        gap { "10px" }
    }) {


        val size = 35
        val y = floor(50 * sqrt(2.0)).toInt()
        val half = y / 2
        div({
            display { flex }
            width { "${y}px" }
            height { "${y}px" }
            children("&.top") {
                css("clip-path: inset(0px 0px ${half}px 0px)")
            }
            children("&.bottom") {
                css("clip-path: inset(${half}px 0px 0px 0px)")
            }
        }) {
            val classesMap = BaseballStateStore.topInning.map { top ->
                mapOf(
                    "top" to top,
                    "bottom" to !top
                )
            }
            classMap(classesMap)
            div({
                width { "${size}px" }
                height { "${size}px" }
                margin { auto }
                background {
                    color { primary.highlight }
                }
                css("transform: rotate(45deg);")
            }) {

            }
        }

        div {
            BaseballStateStore.inning.asText()
        }

        div({
            color {
                "red"
            }
            display { none }
            children("&.final") {
                display { unset }
            }
        }) {
            val classesMap = BaseballStateStore.final.map {
                mapOf(
                    "final" to it
                )
            }
            classMap(classesMap)
            +"F"
        }
    }
}