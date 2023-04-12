package com.copperleaf.ballast.examples.ui.counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.android.databinding.FragmentCounterBinding
import com.copperleaf.ballast.examples.injector.AndroidInjector

class CounterFragment : Fragment() {

    private val injector: AndroidInjector = MainApplication.getInstance().injector
    private val vm: CounterViewModel by viewModels {
        viewModelFactory {
            initializer {
                injector.counterViewModel(createSavedStateHandle(), null, null)
            }
        }
    }
    private var binding: FragmentCounterBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentCounterBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // handle back-button presses to perform navigation
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vm.trySend(CounterContract.Inputs.GoBack)
            }
        })

        // Collect the state and react to events during the Fragment's Lifecycle RESUMED state
        vm.runOnLifecycle(viewLifecycleOwner, injector.counterEventHandler(this)) { state ->
            binding?.updateWithState(state) { event -> vm.trySend(event) }
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
        toolbar.setNavigationOnClickListener {
            postInput(CounterContract.Inputs.GoBack)
        }

        layoutCounter.btnSubtract.setOnClickListener {
            postInput(CounterContract.Inputs.Decrement(1))
        }
        layoutCounter.btnAdd.setOnClickListener {
            postInput(CounterContract.Inputs.Increment(1))
        }

        layoutCounter.tvValue.text = state.count.toString()
    }
}
