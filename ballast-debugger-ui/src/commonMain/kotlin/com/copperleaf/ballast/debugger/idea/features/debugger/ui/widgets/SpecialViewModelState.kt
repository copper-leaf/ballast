@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.settings.DebuggerUiSettings
import com.copperleaf.ballast.debugger.models.BallastStateSnapshot
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrNull

@Composable
internal fun ColumnScope.SpecialViewModelState(
    currentState: BallastStateSnapshot?,
    settings: Cached<DebuggerUiSettings>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    if (settings.getCachedOrNull()?.alwaysShowCurrentState == true) {
        if(currentState != null) {
            Text("Current State")
            Divider()
            StateDetails(currentState, postInput)
        }
    }
}
