package com.copperleaf.ballast.examples.ui.undo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.ballast.examples.databinding.FragmentUndoBinding
import com.copperleaf.ballast.examples.injector.AndroidInjector
import com.copperleaf.ballast.undo.DefaultUndoController
import com.copperleaf.ballast.undo.UndoController
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalBallastApi::class)
class UndoFragment : Fragment() {

    private val undoController: UndoController<
        UndoContract.Inputs,
        UndoContract.Events,
        UndoContract.State> = DefaultUndoController()
    private val injector: AndroidInjector = MainApplication.getInstance().injector
    private val vm: UndoViewModel by viewModels {
        viewModelFactory {
            initializer {
                injector.undoViewModel(undoController)
            }
        }
    }
    private var binding: FragmentUndoBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentUndoBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // handle back-button presses to perform navigation
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vm.trySend(UndoContract.Inputs.GoBack)
            }
        })

        // Collect the state and react to events during the Fragment's Lifecycle RESUMED state
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                combine(
                    vm.observeStates(),
                    undoController.isUndoAvailable,
                    undoController.isRedoAvailable,
                ) { a, b, c -> Triple(a, b, c) }
                    .onEach { (a, b, c) ->
                        binding?.updateWithState(a, b, c) { event -> vm.trySend(event) }
                    }
                    .launchIn(this)
            }
        }
        vm.attachEventHandlerOnLifecycle(viewLifecycleOwner, injector.undoEventHandler(this, undoController))

        binding!!.etTextField.doAfterTextChanged {
            vm.trySend(UndoContract.Inputs.UpdateText(it.toString()))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun FragmentUndoBinding.updateWithState(
        state: UndoContract.State,
        isUndoAvailable: Boolean,
        isRedoAvailable: Boolean,
        postInput: (UndoContract.Inputs) -> Unit
    ) {
        toolbar.setNavigationOnClickListener {
            postInput(UndoContract.Inputs.GoBack)
        }
        if (etTextField.text.toString() != state.text) {
            etTextField.setText(state.text)
        }

        btnUndo.isEnabled = isUndoAvailable
        btnUndo.setOnClickListener { postInput(UndoContract.Inputs.Undo) }
        btnRedo.isEnabled = isRedoAvailable
        btnRedo.setOnClickListener { postInput(UndoContract.Inputs.Redo) }
    }
}
