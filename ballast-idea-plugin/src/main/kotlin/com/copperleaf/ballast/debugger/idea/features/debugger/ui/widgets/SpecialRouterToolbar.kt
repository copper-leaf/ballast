@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrNull

@Composable
fun ColumnScope.SpecialRouterToolbar(
    url: String?,
    settings: Cached<IntellijPluginSettingsSnapshot>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    if (settings.getCachedOrNull()?.showCurrentRoute == true) {
        Row {
            TextField(
                value = url ?: "Current route not found",
                onValueChange = { }, // ignore
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text("Current Route") }
            )
        }
    }
}
