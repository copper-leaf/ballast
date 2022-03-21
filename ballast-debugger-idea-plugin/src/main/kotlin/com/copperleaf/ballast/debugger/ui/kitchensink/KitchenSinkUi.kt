package com.copperleaf.ballast.debugger.ui.kitchensink

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkContract

object KitchenSinkUi {

    @Composable
    fun ui(
        uiState: KitchenSinkContract.State,
        postInput: (KitchenSinkContract.Inputs) -> Unit,
    ) {
        Column {
            Text("State", style = MaterialTheme.typography.h5)
            Divider()
            Text("${uiState.infiniteCounter}")
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (uiState.loading) {
                    CircularProgressIndicator()
                }
            }

            Text("Actions", style = MaterialTheme.typography.h5)
            Divider()

            Text("Inputs", style = MaterialTheme.typography.h6)
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.LongRunningInput()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("LongRunningInput")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningInput) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ErrorRunningInput")
            }

            Text("Events", style = MaterialTheme.typography.h6)
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.LongRunningEvent) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("LongRunningEvent")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningEvent) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ErrorRunningEvent")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.CloseKitchenSinkWindow) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("CloseKitchenSinkWindow")
            }

            Text("SideEffects", style = MaterialTheme.typography.h6)
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.LongRunningSideEffect) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("LongRunningSideEffect")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningSideEffect) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ErrorRunningSideEffect")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.InfiniteSideEffect) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("InfiniteSideEffect")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.CancelInfiniteSideEffect) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("CancelInfiniteSideEffect")
            }
        }
    }
}
