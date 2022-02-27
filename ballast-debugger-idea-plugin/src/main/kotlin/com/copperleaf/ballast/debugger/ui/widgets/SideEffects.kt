package com.copperleaf.ballast.debugger.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.copperleaf.ballast.debugger.models.BallastSideEffectState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerContract
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.removeFraction
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun SideEffectList(
    uiState: DebuggerContract.State,
    viewModelState: BallastViewModelState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SplitPane(
        splitPaneState = uiState.eventsPanePercentage,
        navigation = {
            items(viewModelState.sideEffects) {
                SideEffectSummary(uiState, it, postInput)
            }
        },
        content = {
            if (uiState.focusedViewModelSideEffect != null) {
                SideEffectDetails(uiState, uiState.focusedViewModelSideEffect, postInput)
            }
        }
    )
}

@Composable
fun SideEffectSummary(
    uiState: DebuggerContract.State,
    sideEffectState: BallastSideEffectState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    val timeSinceLastSeen: Duration = (LocalTimer.current - sideEffectState.firstSeen)
        .removeFraction(DurationUnit.SECONDS)

    ListItem(
        modifier = Modifier
            .clickable {
                postInput(
                    DebuggerContract.Inputs.FocusEvent(
                        connectionId = sideEffectState.connectionId,
                        viewModelName = sideEffectState.viewModelName,
                        eventUuid = sideEffectState.uuid,
                    )
                )
            },
        text = { Text(sideEffectState.key) },
        overlineText = { Text(sideEffectState.status.toString()) },
        secondaryText = { Text("${sideEffectState.restartState} - Sent $timeSinceLastSeen ago") },
        trailing = {
            Box {
                if (sideEffectState.status == BallastSideEffectState.Status.Running) {
                    CircularProgressIndicator()
                }
            }
        },
    )
}

@Composable
fun SideEffectDetails(
    uiState: DebuggerContract.State,
    sideEffectState: BallastSideEffectState,
    postInput: (DebuggerContract.Inputs) -> Unit,
) {
    SelectionContainer {
        Column {
            Text(sideEffectState.key)

            (sideEffectState.status as? BallastSideEffectState.Status.Error)?.let {
                Divider()
                Text(it.stacktrace, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
