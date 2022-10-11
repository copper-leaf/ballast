package com.copperleaf.ballast.examples.undo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.undo.UndoController

object UndoComposeUi {

    @Composable
    public fun Content(
        undoController: UndoController<UndoContract.Inputs, UndoContract.Events, UndoContract.State>,
        uiState: UndoContract.State,
        postInput: (UndoContract.Inputs) -> Unit,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val isUndoEnabled by undoController.isUndoAvailable.collectAsState(false)
                Button(
                    onClick = { undoController.undo() },
                    enabled = isUndoEnabled,
                ) {
                    Icon(Icons.Default.Undo, "Undo")
                    Text("Undo")
                }
                val isRedoEnabled by undoController.isRedoAvailable.collectAsState(false)
                Button(
                    onClick = { undoController.redo() },
                    enabled = isRedoEnabled,
                ) {
                    Icon(Icons.Default.Redo, "Redo")
                    Text("Redo")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = uiState.text,
                    onValueChange = {
                        postInput(UndoContract.Inputs.UpdateText(it))
                    }
                )
            }
        }
    }
}
