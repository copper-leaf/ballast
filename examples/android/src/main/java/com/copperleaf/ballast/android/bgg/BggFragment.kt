package com.copperleaf.ballast.android.bgg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.copperleaf.ballast.examples.bgg.models.HotListType
import com.copperleaf.ballast.examples.bgg.ui.BggContract
import com.copperleaf.ballast.examples.bgg.ui.BggEventHandler
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrEmptyList
import com.copperleaf.ballast.repository.cache.isLoading

class BggFragment : Fragment() {

    val eventHandler = BggEventHandler()
    val vm: BggViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    val uiState by vm.observeStates().collectAsState()

                    Column(Modifier.padding(16.dp)) {
                        Content(uiState) { vm.trySend(it) }
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.attachEventHandler(this, eventHandler)
    }

    @Composable
    private fun Content(
        uiState: BggContract.State,
        postInput: (BggContract.Inputs) -> Unit,
    ) {
        if (uiState.bggHotList !is Cached.NotLoaded && uiState.bggHotList.isLoading()) {
            CircularProgressIndicator()
        }
        var forceRefresh by remember { mutableStateOf(false) }

        var dropdownExpanded by remember { mutableStateOf(false) }
        Button(onClick = { dropdownExpanded = true }) {
            Text(uiState.bggHotListType.displayName)
        }
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
        ) {
            HotListType.values().forEach {
                DropdownMenuItem(
                    onClick = {
                        postInput(BggContract.Inputs.ChangeHotListType(it))
                        dropdownExpanded = false
                    },
                ) {
                    Text(it.displayName)
                }
            }
        }

        Row {
            Checkbox(checked = forceRefresh, onCheckedChange = { forceRefresh = it })
            Text("Force refresh")
        }

        Button(onClick = { postInput(BggContract.Inputs.FetchHotList(forceRefresh)) }) {
            Text("Fetch HotList")
        }

        Divider()

        uiState
            .bggHotList
            .getCachedOrEmptyList()
            .forEach {
                Text("${it.rank}: ${it.name}")
                if (it.yearPublished != null) {
                    Text("Published ${it.yearPublished}")
                }
                Divider()
            }
    }
}
