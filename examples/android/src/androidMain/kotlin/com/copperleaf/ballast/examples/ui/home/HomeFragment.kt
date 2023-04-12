package com.copperleaf.ballast.examples.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.android.databinding.FragmentHomeBinding
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.navigation.routing.Floating
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions

@Suppress("UNUSED_PARAMETER")
class HomeFragment : Fragment() {

    private val router by lazy { MainApplication.getInstance().injector.router() }
    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentHomeBinding
            .inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Collect the state and react to events during the Fragment's Lifecycle RESUMED state
        router.observeStatesOnLifecycle(this) { state ->
            binding?.updateWithState(state) { input -> router.trySend(input) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun FragmentHomeBinding.updateWithState(
        state: RouterContract.State<BallastExamples>,
        postInput: (RouterContract.Inputs<BallastExamples>) -> Unit
    ) {
        // counter
        btnCounter.setOnClickListener {
            postInput(
                RouterContract.Inputs.GoToDestination(
                    BallastExamples.Counter
                        .directions()
                        .build(),
                    extraAnnotations = if(cbFloating.isChecked) setOf(Floating) else emptySet(),
                )
            )
        }

        // scorekeeper
        btnScorekeeper.setOnClickListener {
            postInput(
                RouterContract.Inputs.GoToDestination(
                    BallastExamples.Scorekeeper
                        .directions()
                        .build(),
                    extraAnnotations = if(cbFloating.isChecked) setOf(Floating) else emptySet(),
                )
            )
        }

        // sync
        btnSync.setOnClickListener {
            postInput(
                RouterContract.Inputs.GoToDestination(
                    BallastExamples.Sync
                        .directions()
                        .build(),
                    extraAnnotations = if(cbFloating.isChecked) setOf(Floating) else emptySet(),
                )
            )
        }

        // undo
        btnUndo.setOnClickListener {
            postInput(
                RouterContract.Inputs.GoToDestination(
                    BallastExamples.Undo
                        .directions()
                        .build(),
                    extraAnnotations = if(cbFloating.isChecked) setOf(Floating) else emptySet(),
                )
            )
        }

        // api call & cache
        btnBgg.setOnClickListener {
            postInput(
                RouterContract.Inputs.GoToDestination(
                    BallastExamples.ApiCall
                        .directions()
                        .build(),
                    extraAnnotations = if(cbFloating.isChecked) setOf(Floating) else emptySet(),
                )
            )
        }

        // kitchen sink
        btnKitchenSink.setOnClickListener {
            postInput(
                RouterContract.Inputs.GoToDestination(
                    BallastExamples.KitchenSink
                        .directions()
                        .build(),
                    extraAnnotations = if(cbFloating.isChecked) setOf(Floating) else emptySet(),
                )
            )
        }
    }
}
