package com.copperleaf.ballast.android.kitchensink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkContract
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkEventHandler

class KitchenSinkFragment : Fragment() {

    val eventHandler = KitchenSinkEventHandler() { }
    val vm: KitchenSinkViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    val uiState by vm.observeStates().collectAsState()

                    Box(Modifier.padding(16.dp)) {
                        Content(uiState) { vm.trySend(it) }
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.attachEventHandler(this, eventHandler)
    }

    @Composable
    fun Content(
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
