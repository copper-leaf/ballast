package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.copperleaf.ballast.debugger.idea.ui.debugger.DebuggerUiContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastEventState
import com.copperleaf.ballast.debugger.models.BallastViewModelState

@Composable
fun ColumnScope.InterceptorsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    interceptors: List<BallastEventState>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.InterceptorsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    interceptors: List<BallastEventState>,
    selectedInterceptor: BallastEventState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.InterceptorDetailsToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    interceptor: BallastEventState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
fun ColumnScope.InterceptorDetails(
    interceptor: BallastEventState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}
