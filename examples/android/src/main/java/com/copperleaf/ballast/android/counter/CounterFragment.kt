package com.copperleaf.ballast.android.counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.copperleaf.ballast.android.databinding.FragmentCounterBinding
import com.copperleaf.ballast.examples.counter.CounterContract
import com.copperleaf.ballast.examples.counter.CounterEventHandler
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CounterFragment : Fragment() {

    val eventHandler = CounterEventHandler()
    val vm: CounterViewModel by viewModels()
    var binding: FragmentCounterBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentCounterBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.attachEventHandler(this, eventHandler)
        // events are sent back to the screen
        lifecycleScope.launchWhenResumed {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                vm.observeStates()
                    .onEach { state -> binding?.updateWithState(state) { vm.trySend(it) } }
                    .launchIn(this)
            }
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
