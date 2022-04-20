package com.copperleaf.ballast.examples.bgg

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.bgg.ui.BggContract
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrEmptyList
import com.copperleaf.ballast.repository.cache.isLoading

object BggComposeUi {

    @Composable
    fun Content(
        // lifting the dropdown to the parent, because DropdownMenu is not available in common Compose Material code
        openDropdown: () -> Unit,
        dropdownContainer: @Composable () -> Unit,

        uiState: BggContract.State,
        postInput: (BggContract.Inputs) -> Unit,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            var forceRefresh by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = { openDropdown() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(uiState.bggHotListType.displayName)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                dropdownContainer()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = forceRefresh, onCheckedChange = { forceRefresh = it })
                Text("Force refresh")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { postInput(BggContract.Inputs.FetchHotList(forceRefresh)) },
            ) {
                Text("Fetch HotList")
            }

            Divider()

            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                LazyColumn {
                    items(
                        uiState
                            .bggHotList
                            .getCachedOrEmptyList()
                    ) {
                        ListItem(
//                            icon = {
//                                // TODO: fetch and display the image here
//                                Box(Modifier.size(48.dp, 48.dp))
//                            },
                            text = { Text("${it.rank}: ${it.name}") },
                            overlineText = if (it.yearPublished != null) {
                                { Text("Published ${it.yearPublished}") }
                            } else null
                        )
                    }
                }

                if (uiState.bggHotList !is Cached.NotLoaded && uiState.bggHotList.isLoading()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
