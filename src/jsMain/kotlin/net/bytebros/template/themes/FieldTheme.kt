package net.bytebros.template.themes

import dev.fritz2.styling.params.ColorProperty
import dev.fritz2.styling.theme.ColorScheme
import dev.fritz2.styling.theme.Colors
import dev.fritz2.styling.theme.DefaultTheme

class FieldTheme: DefaultTheme() {
    override val colors: Colors = object : Colors by super.colors {
        override val primary =
            ColorScheme(
                main = "#4B5010",
                mainContrast = "#FFFFFF",
                highlight = "#FFFFFF",
                highlightContrast = "#4B5010"
            )
    }
}