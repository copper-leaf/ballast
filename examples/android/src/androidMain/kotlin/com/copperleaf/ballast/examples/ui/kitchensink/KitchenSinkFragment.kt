package com.copperleaf.ballast.examples.ui.kitchensink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.copperleaf.android.R
import com.copperleaf.android.databinding.FragmentKitchenSinkBinding
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.ballast.examples.injector.AndroidInjector
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.optionalEnumQuery
import com.copperleaf.ballast.navigation.toDestinationParameters

class KitchenSinkFragment : Fragment(), Destination.ParametersProvider {

    override val parameters: Destination.Parameters by lazy { requireArguments().toDestinationParameters() }
    private val inputStrategy by optionalEnumQuery(InputStrategySelection::valueOf)

    private val injector: AndroidInjector = MainApplication.getInstance().injector
    private val vm: KitchenSinkViewModel by viewModels {
        viewModelFactory {
            initializer {
                injector.kitchenSinkViewModel(inputStrategy ?: InputStrategySelection.Lifo)
            }
        }
    }
    private var binding: FragmentKitchenSinkBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentKitchenSinkBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // handle back-button presses to perform navigation
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vm.trySend(KitchenSinkContract.Inputs.CloseKitchenSinkWindow)
            }
        })

        // Collect the state and react to events during the Fragment's Lifecycle RESUMED state
        vm.runOnLifecycle(viewLifecycleOwner, injector.kitchenSinkEventHandler(this)) { state ->
            binding?.updateWithState(state) { event -> vm.trySend(event) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun FragmentKitchenSinkBinding.updateWithState(
        state: KitchenSinkContract.State,
        postInput: (KitchenSinkContract.Inputs) -> Unit
    ) {
        tvCounter.text = "Counter: ${state.infiniteCounter}"
        tvCompletedInputs.text = "Completed Inputs: ${state.completedInputCounter}"
        if (state.loading) {
            progress.show()
        } else {
            progress.hide()
        }

        btnLongRunningInput.setOnClickListener { postInput(KitchenSinkContract.Inputs.LongRunningInput) }
        btnErrorRunningInput.setOnClickListener { postInput(KitchenSinkContract.Inputs.ErrorRunningInput) }
        btnLongRunningEvent.setOnClickListener { postInput(KitchenSinkContract.Inputs.LongRunningEvent) }
        btnErrorRunningEvent.setOnClickListener { postInput(KitchenSinkContract.Inputs.ErrorRunningEvent) }
        btnCloseKitchenSinkWindow.setOnClickListener { postInput(KitchenSinkContract.Inputs.CloseKitchenSinkWindow) }
        btnLongRunningSideJob.setOnClickListener { postInput(KitchenSinkContract.Inputs.LongRunningSideJob) }
        btnErrorRunningSideJob.setOnClickListener { postInput(KitchenSinkContract.Inputs.ErrorRunningSideJob) }
        btnInfiniteSideJob.setOnClickListener { postInput(KitchenSinkContract.Inputs.InfiniteSideJob) }
        btnCancelInfiniteSideJob.setOnClickListener { postInput(KitchenSinkContract.Inputs.CancelInfiniteSideJob) }
        btnShutDownGracefully.setOnClickListener { postInput(KitchenSinkContract.Inputs.ShutDownGracefully) }

        btnInfiniteSideJob.isVisible = !state.infiniteSideJobRunning
        btnCancelInfiniteSideJob.isVisible = state.infiniteSideJobRunning

        toolbar.title = "Kitchen Sink (${state.inputStrategy.name})"
        toolbar.setNavigationOnClickListener {
            postInput(KitchenSinkContract.Inputs.CloseKitchenSinkWindow)
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.lifo -> {
                    postInput(KitchenSinkContract.Inputs.ChangeInputStrategy(InputStrategySelection.Lifo))
                    true
                }

                R.id.fifo -> {
                    postInput(KitchenSinkContract.Inputs.ChangeInputStrategy(InputStrategySelection.Fifo))
                    true
                }

                R.id.parallel -> {
                    postInput(KitchenSinkContract.Inputs.ChangeInputStrategy(InputStrategySelection.Parallel))
                    true
                }

                else -> {
                    false
                }
            }
        }
    }
}
