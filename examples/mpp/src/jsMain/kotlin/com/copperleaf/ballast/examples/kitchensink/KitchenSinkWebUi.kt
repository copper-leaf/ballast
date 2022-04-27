package com.copperleaf.ballast.examples.kitchensink

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.examples.util.bulma.BulmaButton
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Progress
import org.jetbrains.compose.web.dom.Text

object KitchenSinkWebUi {
    @Composable
    public fun Content(
        uiState: KitchenSinkContract.State,
        postInput: (KitchenSinkContract.Inputs) -> Unit,
    ) {
        Text("State")
        Hr { }
        Text("Completed Input: ${uiState.completedInputCounter}")
        Br { }
        Text("Counter: ${uiState.infiniteCounter}")
        Br { }
        if (uiState.loading) {
            Progress { }
        }

        Text("Actions")
        Hr { }

        Text("Inputs")
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.LongRunningInput()) },
        ) {
            Text("LongRunningInput")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningInput) },
        ) {
            Text("ErrorRunningInput")
        }
        Br { }

        Text("Events")
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.LongRunningEvent) },
        ) {
            Text("LongRunningEvent")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningEvent) },
        ) {
            Text("ErrorRunningEvent")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.CloseKitchenSinkWindow) },
        ) {
            Text("CloseKitchenSinkWindow")
        }
        Br { }

        Text("SideJobs")
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.LongRunningSideJob) },
        ) {
            Text("LongRunningSideJob")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningSideJob) },
        ) {
            Text("ErrorRunningSideJob")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.InfiniteSideJob) },
        ) {
            Text("InfiniteSideJob")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.CancelInfiniteSideJob) },
        ) {
            Text("CancelInfiniteSideJob")
        }
        Br { }
    }
}
