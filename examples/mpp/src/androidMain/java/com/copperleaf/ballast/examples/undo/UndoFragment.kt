package com.copperleaf.ballast.examples.undo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.ballast.examples.util.BallastViewModelFactory

class UndoFragment : Fragment() {

    val vm: UndoViewModel by viewModels { BallastViewModelFactory(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    val injector = remember { MainApplication.getInstance().injector }
                    val undoController = remember(injector) { injector.undoController }
                    val uiState by vm.observeStates().collectAsState()

                    LaunchedEffect(vm) {
                        vm.attachEventHandler(this, UndoEventHandler())
                    }

                    UndoComposeUi.Content(undoController, uiState) { vm.trySend(it) }
                }
            }
    }
}
