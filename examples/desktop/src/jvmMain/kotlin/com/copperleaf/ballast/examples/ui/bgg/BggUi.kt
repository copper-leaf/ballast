package com.copperleaf.ballast.examples.ui.bgg

import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.api.models.HotListType
import com.copperleaf.ballast.examples.injector.ComposeDesktopInjector
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrEmptyList
import com.copperleaf.ballast.repository.cache.isLoading

object BggUi {

    @Composable
    fun Content(injector: ComposeDesktopInjector) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.bggViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    fun Content(
        uiState: BggContract.State,
        postInput: (BggContract.Inputs) -> Unit,
    ) {
        Column(Modifier.fillMaxSize()) {
            var dropdownExpanded by remember { mutableStateOf(false) }
            var forceRefresh by remember { mutableStateOf(false) }

            TopAppBar(
                contentPadding = PaddingValues(0.dp),
                content = {
                    Row(
                        Modifier.fillMaxHeight().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProvideTextStyle(value = MaterialTheme.typography.h6) {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                                Text("API Call & Cache", overflow = TextOverflow.Ellipsis, maxLines = 1)
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
                                modifier = Modifier.wrapContentWidth().clickable { dropdownExpanded = true },
                                overlineText = { Text("BoardGameGeek HotList Type") },
                                trailing = { Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "") },
                                text = { Text(uiState.bggHotListType.displayName) },
                            )

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                HotListType.values().forEach { strategy ->
                                    DropdownMenuItem(
                                        onClick = {
                                            dropdownExpanded = false
                                            postInput(BggContract.Inputs.ChangeHotListType(strategy))
                                        },
                                        content = { Text(strategy.name) },
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val scrollState = rememberLazyListState()
                LazyColumn(state = scrollState) {
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
                VerticalScrollbar(ScrollbarAdapter(scrollState), Modifier.align(Alignment.CenterEnd))

                if (uiState.bggHotList !is Cached.NotLoaded && uiState.bggHotList.isLoading()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            Divider()

            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                ListItem(
                    modifier = Modifier.fillMaxHeight().weight(1f).clickable { forceRefresh = !forceRefresh },
                    icon = { Checkbox(checked = forceRefresh, onCheckedChange = null) },
                    text = { Text("Force refresh") },
                )
                Box(Modifier.padding(horizontal = 16.dp)) {
                    Button(onClick = { postInput(BggContract.Inputs.FetchHotList(forceRefresh)) }) {
                        Text("Fetch HotList")
                    }
                }
            }
        }
    }
}
