package com.copperleaf.ballast.examples.scheduler.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.examples.scheduler.memory.InMemorySchedulesUi
import com.copperleaf.ballast.examples.scheduler.persistent.PersistentSchedulesUi

@ExperimentalMaterial3Api
object SchedulerExampleLayout {

    @Composable
    fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm: SchedulerExampleLayoutViewModel = remember(viewModelCoroutineScope) {
            SchedulerExampleLayoutViewModel(viewModelCoroutineScope)
        }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        uiState: SchedulerExampleLayoutContract.State,
        postInput: (SchedulerExampleLayoutContract.Inputs) -> Unit,
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    LayoutTabs.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = uiState.tab == tab,
                            onClick = { postInput(SchedulerExampleLayoutContract.Inputs.ChangeTab(tab)) },
                            label = { Text(tab.name) },
                            icon = {}
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
            ) {
                when (uiState.tab) {
                    LayoutTabs.InMemory -> InMemorySchedulesUi.Content(this)
                    LayoutTabs.Persistent -> PersistentSchedulesUi.Content(this)
                }
            }
        }
    }
}
