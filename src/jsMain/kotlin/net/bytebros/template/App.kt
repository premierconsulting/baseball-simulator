package net.bytebros.template

import dev.fritz2.styling.theme.render
import net.bytebros.template.components.baseballState
import net.bytebros.template.themes.FieldTheme


fun main() {
    render(FieldTheme(), "#target") {
        baseballState()
    }
}