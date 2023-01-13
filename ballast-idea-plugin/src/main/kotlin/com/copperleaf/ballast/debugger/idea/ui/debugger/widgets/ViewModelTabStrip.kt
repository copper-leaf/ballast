@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.navigation.routing.Destination

@Composable
fun Destination.Match<DebuggerRoute>.ViewModelTabStrip(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {

}
