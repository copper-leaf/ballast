@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.utils.maybeFilter
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastInterceptorState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.stringPath

@Composable
internal fun ColumnScope.InterceptorsListToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    interceptors: List<BallastInterceptorState>,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
internal fun ColumnScope.InterceptorsList(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    interceptors: List<BallastInterceptorState>,
    focusedInterceptor: BallastInterceptorState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()

        // the list of all Connections
        LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
            items(interceptors) {
                InterceptorSummary(it, focusedInterceptor, postInput)
            }
        }

        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
        )
    }
}

@Composable
internal fun ColumnScope.InterceptorDetailsToolbar(
    connection: BallastConnectionState?,
    viewModel: BallastViewModelState?,
    interceptor: BallastInterceptorState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Composable
internal fun ColumnScope.InterceptorDetails(
    interceptor: BallastInterceptorState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
}

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun InterceptorSummary(
    interceptorState: BallastInterceptorState,
    focusedInterceptor: BallastInterceptorState?,
    postInput: (DebuggerUiContract.Inputs) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .onHoverState { Modifier.highlight() }
            .then(
                if (focusedInterceptor?.uuid == interceptorState.uuid) {
                    Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                } else {
                    Modifier
                }
            )
            .clickable {
                postInput(
                    DebuggerUiContract.Inputs.Navigate(
                        DebuggerRoute.ViewModelInterceptorDetails
                            .directions()
                            .pathParameter("connectionId", interceptorState.connectionId)
                            .pathParameter("viewModelName", interceptorState.viewModelName)
                            .pathParameter("interceptorUuid", interceptorState.uuid)
                            .build()
                    )
                )
            },
        text = { Text(interceptorState.type) },
        overlineText = { Text(interceptorState.status.toString()) },
    )
}

// Data for Interceptors
// ---------------------------------------------------------------------------------------------------------------------

@Composable
internal fun rememberViewModelInterceptorList(
    viewModel: BallastViewModelState?,
    searchText: String,
): State<List<BallastInterceptorState>> {
    return viewModelValue {
        viewModel?.interceptors?.maybeFilter(searchText) {
            listOf(it.type, it.toStringValue)
        } ?: emptyList()
    }
}

@Composable
internal fun Destination.ParametersProvider.rememberSelectedViewModelInterceptor(
    viewModel: BallastViewModelState?,
): State<BallastInterceptorState?> {
    return viewModelValue {
        val interceptorUuid: String by stringPath()
        viewModel?.interceptors?.find { it.uuid == interceptorUuid }
    }
}
