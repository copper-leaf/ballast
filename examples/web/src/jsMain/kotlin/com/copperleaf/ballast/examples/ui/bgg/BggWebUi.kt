package com.copperleaf.ballast.examples.ui.bgg

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.copperleaf.ballast.examples.api.models.HotListType
import com.copperleaf.ballast.examples.injector.ComposeWebInjector
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaCheckbox
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaPanel
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaProgress
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaSelect
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrEmptyList
import com.copperleaf.ballast.repository.cache.isLoading
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

object BggWebUi {

    @Composable
    fun Content(injector: ComposeWebInjector) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.bggViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        uiState: BggContract.State,
        postInput: (BggContract.Inputs) -> Unit,
    ) {
        if (uiState.bggHotList !is Cached.NotLoaded && uiState.bggHotList.isLoading()) {
            BulmaProgress()
        }

        var forceRefresh by remember { mutableStateOf(false) }

        BulmaSelect(
            fieldName = "HotList Type",
            items = HotListType.values().toList(),
            itemValue = { it.name },
            selectedValue = uiState.bggHotListType,
            onValueChange = { postInput(BggContract.Inputs.ChangeHotListType(it)) },
            itemContent = { Text(it.displayName) }
        )
        BulmaCheckbox(
            fieldName = "Force Refresh",
            value = forceRefresh,
            onValueChange = { forceRefresh = it }
        )
        BulmaButton(
            onClick = { postInput(BggContract.Inputs.FetchHotList(forceRefresh)) }
        ) { Text("Fetch HotList") }

        Hr { }

        uiState
            .bggHotList
            .getCachedOrEmptyList()
            .forEach {
                BulmaPanel({ Text("${it.rank}: ${it.name}") }) {
                    if (it.yearPublished != null) {
                        Text("Published ${it.yearPublished}")
                    }

                    if (it.thumbnail.isNotBlank()) {
                        Img(src = it.thumbnail)
                    }
                }
            }
    }
}
