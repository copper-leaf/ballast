package com.copperleaf.ballast.examples.web.internal.bulma

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.dom.CheckboxInput
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Progress
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text


@Composable
fun BulmaFormField(
    fieldName: String,
    content: @Composable () -> Unit,
) {
    Div({ classes("field") }) {
        Label(attrs = { classes("label") }) { Text(fieldName) }
        Div({ classes("control") }) {
            content()
        }
    }
}


@Composable
fun <T : Any> BulmaSelect(
    fieldName: String,
    items: List<T>,
    itemValue: (T) -> String,

    selectedValue: T?,
    onValueChange: (T) -> Unit,

    color: BulmaColor = BulmaColor.Primary,
    size: BulmaSize = BulmaSize.Default,

    itemContent: @Composable (T) -> Unit,
) {
    BulmaFormField(fieldName) {
        Div({
            ClassList {
                this += "select"

                if (color != BulmaColor.Default) {
                    this += color.cssClass
                }
                if (size != BulmaSize.Default) {
                    this += size.cssClass
                }
            }
        }) {
            Select(
                attrs = {
                    onChange { item ->
                        val selectedItem = items.first { itemValue(it) == item.value }
                        onValueChange(selectedItem)
                        item.stopPropagation()
                    }
                }
            ) {
                items.forEach { item ->
                    Option(
                        value = itemValue(item),
                        attrs = {
                            if (item == selectedValue) {
                                selected()
                            }
                        }
                    ) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}

@Composable
fun BulmaInput(
    fieldName: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    BulmaFormField(fieldName) {
        Input(type = InputType.Text) {
            classes("input")
            value(value)
            onInput { event -> onValueChange(event.value) }
        }
    }
}

@Composable
fun BulmaCheckbox(
    fieldName: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    Label(attrs = { classes("label") }) {
        CheckboxInput(
            checked = value,
            attrs = {
                onChange { event ->
                    onValueChange(event.value)
                    event.stopPropagation()
                }
            }
        )
        Text(fieldName)
    }
}

@Composable
fun BulmaProgress() {
    Progress({
        ClassList {
            this += "progress"
            this += "is-small"
        }
    })
}
