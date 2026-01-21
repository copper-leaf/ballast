package com.copperleaf.ballast.examples.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.enums.EnumEntries

@Composable
fun <T : Enum<T>> DropdownSelector(
    value: T,
    onValueChange: (T) -> Unit,
    allEnumValues: EnumEntries<T>,
    label: @Composable () -> Unit,
    formatEnumValue: (T) -> String = { it.toString() },
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = formatEnumValue(value),
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = {
                IconButton({ isMenuOpen = true }) {
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }
        )
        DropdownMenu(
            expanded = isMenuOpen,
            onDismissRequest = { isMenuOpen = false },
        ) {
            allEnumValues.forEach { enumValue ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(enumValue)
                        isMenuOpen = false
                    },
                    text = {
                        Text(formatEnumValue(enumValue))
                    }
                )
            }
        }
    }
}
