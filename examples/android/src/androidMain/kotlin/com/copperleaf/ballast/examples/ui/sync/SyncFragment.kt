package com.copperleaf.ballast.examples.ui.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.android.databinding.FragmentSyncBinding
import com.copperleaf.android.databinding.IncludeCounterBinding
import com.copperleaf.ballast.examples.injector.AndroidInjector
import com.copperleaf.ballast.examples.ui.counter.CounterContract
import com.copperleaf.ballast.sync.DefaultSyncConnection
import com.copperleaf.ballast.sync.InMemorySyncAdapter

class SyncFragment : Fragment() {

    private val injector: AndroidInjector = MainApplication.getInstance().injector
    private var binding: FragmentSyncBinding? = null
    private val syncAdapter = InMemorySyncAdapter<
            CounterContract.Inputs,
            CounterContract.Events,
            CounterContract.State>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentSyncBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm = injector.counterViewModel(null, DefaultSyncConnection.ClientType.Source, syncAdapter)

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

        binding!!.replica1.setupLocalState(DefaultSyncConnection.ClientType.Replica)
        binding!!.replica2.setupLocalState(DefaultSyncConnection.ClientType.Replica)
        binding!!.replica3.setupLocalState(DefaultSyncConnection.ClientType.Replica)

        binding!!.spectator1.setupLocalState(DefaultSyncConnection.ClientType.Spectator)
        binding!!.spectator2.setupLocalState(DefaultSyncConnection.ClientType.Spectator)
        binding!!.spectator3.setupLocalState(DefaultSyncConnection.ClientType.Spectator)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun FragmentSyncBinding.updateWithState(
        state: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit
    ) {
        toolbar.setNavigationOnClickListener {
            postInput(CounterContract.Inputs.GoBack)
        }

        source.updateWithLocalState(state, postInput)
    }

    private fun IncludeCounterBinding.setupLocalState(clientType: DefaultSyncConnection.ClientType) {
        val localVm = injector.counterViewModel(null, clientType, syncAdapter)

        localVm.runOnLifecycle(viewLifecycleOwner, injector.counterEventHandler(this@SyncFragment)) { state ->
            updateWithLocalState(state) { event -> localVm.trySend(event) }
        }
    }

    private fun IncludeCounterBinding.updateWithLocalState(
        state: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit
    ) {
        btnSubtract.setOnClickListener {
            postInput(CounterContract.Inputs.Decrement(1))
        }
        btnAdd.setOnClickListener {
            postInput(CounterContract.Inputs.Increment(1))
        }

        tvValue.text = state.count.toString()
    }
}
