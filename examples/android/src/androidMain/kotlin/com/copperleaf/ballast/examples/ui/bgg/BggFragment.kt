package com.copperleaf.ballast.examples.ui.bgg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.android.R
import com.copperleaf.android.databinding.FragmentBggBinding
import com.copperleaf.ballast.examples.api.models.HotListType
import com.copperleaf.ballast.examples.injector.AndroidInjector
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrEmptyList
import com.copperleaf.ballast.repository.cache.isLoading

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class BggFragment : Fragment() {

    private val injector: AndroidInjector = MainApplication.getInstance().injector
    private val vm: BggViewModel by viewModels {
        viewModelFactory {
            initializer {
                injector.bggViewModel()
            }
        }
    }
    private var binding: FragmentBggBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentBggBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // handle back-button presses to perform navigation
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                vm.trySend(BggContract.Inputs.GoBack)
            }
        })

        // Collect the state and react to events during the Fragment's Lifecycle RESUMED state
        vm.runOnLifecycle(viewLifecycleOwner, injector.bggEventHandler(this)) { state ->
            binding?.updateWithState(state) { event -> vm.trySend(event) }
        }

        binding!!.etHotlistType.setAdapter(
            ArrayAdapter(requireContext(), R.layout.dropdown_item, HotListType.values())
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun FragmentBggBinding.updateWithState(
        state: BggContract.State,
        postInput: (BggContract.Inputs) -> Unit
    ) {
        toolbar.setNavigationOnClickListener {
            postInput(BggContract.Inputs.GoBack)
        }

        etHotlistType.setOnItemClickListener { parent, view, position, id ->
            postInput(
                BggContract.Inputs.ChangeHotListType(
                    HotListType.values()[position]
                )
            )
        }
        cbForceRefresh.setOnClickListener {
            postInput(BggContract.Inputs.SetForceRefresh(!state.forceRefresh))
        }
        cbForceRefresh.isChecked = state.forceRefresh
        btnFetch.setOnClickListener {
            postInput(BggContract.Inputs.FetchHotList(state.forceRefresh))
        }

        if (state.bggHotList.isLoading() && state.bggHotList !is Cached.NotLoaded) {
            progress.show()
        } else {
            progress.hide()
        }
        rvHotListItems.adapter = BggAdapter(state.bggHotList.getCachedOrEmptyList())
    }
}
