@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.navigation.routing.Destination

@Composable
internal fun Destination.Match<DebuggerRoute>.ViewModelTabStrip(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    if (connection == null || viewModel == null) {
        return
    }

    val selectedTab: ViewModelContentTab? = ViewModelContentTab.fromRoute(originalRoute)

    ScrollableTabRow(
        selectedTabIndex = selectedTab?.ordinal ?: 0,
    ) {
        ViewModelContentTab.values().forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { postInput(tab.navigate(connection, viewModel)) },
                icon = { Icon(tab.icon, tab.text) },
                enabled = tab.isEnabled(connection),
                text = { Text(tab.text) },
                selectedContentColor = if (tab.isActive(viewModel)) {
                    MaterialTheme.colors.secondary
                } else {
                    LocalContentColor.current
                },
            )
        }
    }
}
