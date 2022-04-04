package com.copperleaf.ballast.examples.web.util.bulma

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div

enum class BulmaButtonFeatures(val cssClass: String) {
    Lighter("is-light"),
    Outlined("is-outlined"),
    Inverted("is-inverted"),
    Rounded("is-rounded"),
    Loading("is-loading"),
    FullWidth("is-fullwidth"),
}

@Composable
fun BulmaButton(
    onClick: () -> Unit,
    color: BulmaColor = BulmaColor.Primary,
    size: BulmaSize = BulmaSize.Default,
    features: Set<BulmaButtonFeatures> = emptySet(),
    isDisabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    Button({
        ClassList {
            this += "button"
            if (color != BulmaColor.Default) {
                this += color.cssClass
            }
            if (size != BulmaSize.Default) {
                this += size.cssClass
            }
            features.forEach {
                this += it.cssClass
            }
            if (isDisabled) {
                disabled()
            }
        }

        onClick {
            onClick()
            it.stopPropagation()
        }
    }) {
        content()
    }
}

object BulmaButtonGroupScope {
    @Composable
    fun Control(
        content: @Composable () -> Unit,
    ) {
        content()
    }
}

@Composable
fun BulmaButtonGroup(
    content: @Composable BulmaButtonGroupScope.() -> Unit,
) {
    Div({
        ClassList {
            this += "buttons"
            this += "has-addons"
            this += "is-centered"
        }
    }) {
        BulmaButtonGroupScope.content()
    }
}
