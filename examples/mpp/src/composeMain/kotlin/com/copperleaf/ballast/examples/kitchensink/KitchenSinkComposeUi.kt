package com.copperleaf.ballast.examples.kitchensink

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object KitchenSinkComposeUi {

    @Composable
    public fun Content(
        uiState: KitchenSinkContract.State,
        postInput: (KitchenSinkContract.Inputs) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("State", style = MaterialTheme.typography.h5)
            Divider()
            Text("Completed Input: ${uiState.completedInputCounter}")
            Text("Counter: ${uiState.infiniteCounter}")
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

            Text("SideJobs", style = MaterialTheme.typography.h6)
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.LongRunningSideJob) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("LongRunningSideJob")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningSideJob) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ErrorRunningSideJob")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.InfiniteSideJob) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("InfiniteSideJob")
            }
            Button(
                onClick = { postInput(KitchenSinkContract.Inputs.CancelInfiniteSideJob) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("CancelInfiniteSideJob")
            }
        }
    }
}
