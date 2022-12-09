package com.copperleaf.ballast.examples.ui.scorekeeper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.ballast.examples.databinding.FragmentScorekeeperBinding
import com.copperleaf.ballast.examples.injector.AndroidInjector

class ScorekeeperFragment : Fragment() {

    private val injector: AndroidInjector = MainApplication.getInstance().injector
    private val vm: ScorekeeperViewModel by viewModels {
        viewModelFactory {
            initializer {
                injector.scorekeeperViewModel()
            }
        }
    }
    private var binding: FragmentScorekeeperBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentScorekeeperBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // handle back-button presses to perform navigation
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vm.trySend(ScorekeeperContract.Inputs.GoBack)
            }
        })

        // Collect the state and react to events during the Fragment's Lifecycle RESUMED state
        vm.runOnLifecycle(viewLifecycleOwner, injector.scorekeeperEventHandler(this)) { state ->
            binding?.updateWithState(state) { event -> vm.trySend(event) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun FragmentScorekeeperBinding.updateWithState(
        state: ScorekeeperContract.State,
        postInput: (ScorekeeperContract.Inputs) -> Unit
    ) {
        toolbar.setNavigationOnClickListener {
            postInput(ScorekeeperContract.Inputs.GoBack)
        }

        rvScorekeeper.adapter = ScorekeeperAdapter(state.players, postInput)
        tilNewPlayer.setEndIconOnClickListener {
            postInput(
                ScorekeeperContract.Inputs.AddPlayer(etNewPlayer.text.toString())
            )
            etNewPlayer.setText("")
        }

        btnAdd1.setOnClickListener { postInput(ScorekeeperContract.Inputs.ChangeScore(1)) }
        btnAdd5.setOnClickListener { postInput(ScorekeeperContract.Inputs.ChangeScore(5)) }
        btnAdd10.setOnClickListener { postInput(ScorekeeperContract.Inputs.ChangeScore(10)) }

        btnSub1.setOnClickListener { postInput(ScorekeeperContract.Inputs.ChangeScore(-1)) }
        btnSub5.setOnClickListener { postInput(ScorekeeperContract.Inputs.ChangeScore(-5)) }
        btnSub10.setOnClickListener { postInput(ScorekeeperContract.Inputs.ChangeScore(-10)) }
    }
}
