package com.copperleaf.ballast.examples.counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.copperleaf.ballast.examples.databinding.FragmentCounterBinding
import com.copperleaf.ballast.examples.util.BallastViewModelFactory

class CounterFragment : Fragment() {

    val eventHandler = CounterEventHandler()
    val vm: CounterViewModel by viewModels { BallastViewModelFactory(this) }
    var binding: FragmentCounterBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentCounterBinding
            .inflate(inflater, container, false)
            .apply {
                composeLayout.setContent {
                    val uiState by vm.observeStates().collectAsState()

                    Box(Modifier.padding(16.dp)) {
                        CounterComposeUi.Content(uiState) { vm.trySend(it) }
                    }
                }
            }
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // events are sent back to the screen during the Fragment's Lifecycle RESUMED state
        vm.attachEventHandlerOnLifecycle(this, eventHandler)

        // Collect the state on the Fragment's Lifecycle RESUMED state, updating the entire UI with each change
        vm.observeStatesOnLifecycle(this) {
            binding?.updateWithState(it) { vm.trySend(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun FragmentCounterBinding.updateWithState(
        state: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit
    ) {
        tvCounter.text = "${state.count}"

        btnDec.setOnClickListener { postInput(CounterContract.Inputs.Decrement(1)) }
        btnInc.setOnClickListener { postInput(CounterContract.Inputs.Increment(1)) }
    }
}
