package com.copperleaf.ballast.examples.kitchensink.controller

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkComposeUi

object KitchenSinkControllerUi {

    @Suppress("UNUSED_PARAMETER")
    @Composable
    fun Content(
        // lifting the dropdown to the parent, because DropdownMenu is not available in common Compose Material code
        openDropdown: () -> Unit,
        dropdownContainer: @Composable () -> Unit,

        uiState: KitchenSinkControllerContract.State,
        postInput: (KitchenSinkControllerContract.Inputs) -> Unit,
    ) {
        Column {
            TopAppBar(
                contentPadding = PaddingValues(0.dp),
                content = {
                    Row(
                        Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProvideTextStyle(value = MaterialTheme.typography.h6) {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                                Text("Kitchen Sink", overflow = TextOverflow.Ellipsis, maxLines = 1)
                            }
                        }
                    }

                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Row(
                            Modifier.fillMaxHeight().weight(1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ListItem(
                                modifier = Modifier.wrapContentWidth().clickable { openDropdown() },
                                overlineText = { Text("Input Strategy") },
                                trailing = { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "") },
                                text = { Text(uiState.inputStrategy.name) },
                            )

                            dropdownContainer()
                        }
                    }
                }
            )

            if (uiState.viewModel != null) {
                val sampleUiState by uiState.viewModel.observeStates().collectAsState()

                KitchenSinkComposeUi.Content(
                    uiState = sampleUiState,
                    postInput = { uiState.viewModel.trySend(it) },
                )
            }
        }
    }

}
