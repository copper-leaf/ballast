package com.copperleaf.ballast.examples.util.bulma

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.value
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement


object ColumnScope {

}

object RowScope {
    @Composable
    fun Column(
        attrs: AttrBuilderContext<HTMLDivElement>? = null,
        span: Int = 1,
        alignment: JustifyContent = JustifyContent.Left,
        content: @Composable ColumnScope.() -> Unit
    ) {
        check(span in 1..12) { "span be in 1..12" }

        Div({
            attrs?.invoke(this)
            style {
                flexDirection(FlexDirection.Column)
                justifyContent(alignment)
            }
            ClassList {
                this += "column"
                this += "is-$span"
                this += "is-flex"
                this += "is-justify-content-${alignment.value}"
                this += "is-flex-direction-column"
            }
        }) {
            ColumnScope.content()
        }
    }
}


@Composable
fun Row(
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    content: @Composable RowScope.() -> Unit
) {
    Div({
        attrs?.invoke(this)
        classes("columns", "is-flex-grow-1")
    }) {
        RowScope.content()
    }
}
