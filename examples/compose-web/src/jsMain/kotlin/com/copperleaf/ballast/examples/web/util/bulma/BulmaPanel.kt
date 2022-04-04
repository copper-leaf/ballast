package com.copperleaf.ballast.examples.web.util.bulma

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.Span

@Composable
fun BulmaPanel(
    headingStart: @Composable () -> Unit,
    headingEnd: @Composable () -> Unit = {},
    alignment: JustifyContent = JustifyContent.Left,
    color: BulmaColor = BulmaColor.Default,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Nav({
        ClassList {
            this += "panel"
            if(color != BulmaColor.Default) {
                this += color.cssClass
            }
        }
        if (onClick != null) {
            onClick {
                onClick()
                it.stopPropagation()
            }
        }
    }) {
        Div({ classes("panel-heading", "is-flex", "is-flex-direction-row") }) {
            Span({
                style { flexGrow(1) }
            }) { headingStart() }
            Span { headingEnd() }
        }
        Div({ classes("panel-block") }) {
            Row {
                Column(alignment = alignment, span = 12) {
                    content()
                }
            }
        }
    }
}
