package com.copperleaf.ballast.debugger.ui.sample

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

object SampleUi {

    @Composable
    fun ui(
        uiState: SampleContract.State,
        postInput: (SampleContract.Inputs) -> Unit,
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
                onClick = { postInput(SampleContract.Inputs.LongRunningInput()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("LongRunningInput")
            }
            Button(
                onClick = { postInput(SampleContract.Inputs.ErrorRunningInput) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ErrorRunningInput")
            }

            Text("Events", style = MaterialTheme.typography.h6)
            Button(
                onClick = { postInput(SampleContract.Inputs.LongRunningEvent) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("LongRunningEvent")
            }
            Button(
                onClick = { postInput(SampleContract.Inputs.ErrorRunningEvent) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ErrorRunningEvent")
            }
            Button(
                onClick = { postInput(SampleContract.Inputs.CloseSampleWindow) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("CloseSampleWindow")
            }

            Text("SideEffects", style = MaterialTheme.typography.h6)
            Button(
                onClick = { postInput(SampleContract.Inputs.LongRunningSideEffect) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("LongRunningSideEffect")
            }
            Button(
                onClick = { postInput(SampleContract.Inputs.ErrorRunningSideEffect) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ErrorRunningSideEffect")
            }
            Button(
                onClick = { postInput(SampleContract.Inputs.InfiniteSideEffect) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("InfiniteSideEffect")
            }
            Button(
                onClick = { postInput(SampleContract.Inputs.CancelInfiniteSideEffect) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("CancelInfiniteSideEffect")
            }
        }
    }
}
