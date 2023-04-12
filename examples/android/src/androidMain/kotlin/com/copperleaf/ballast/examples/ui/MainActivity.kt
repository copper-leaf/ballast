package com.copperleaf.ballast.examples.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.copperleaf.ballast.examples.MainApplication
import com.copperleaf.android.databinding.ActivityMainBinding
import com.copperleaf.ballast.examples.injector.AndroidInjector
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.examples.router.BallastExamplesRouterEventHandler
import com.copperleaf.ballast.navigation.routing.RouterContract

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {

    private val injector: AndroidInjector = MainApplication.getInstance().injector
    private val vm: BallastExamplesRouter by viewModels {
        viewModelFactory {
            initializer {
                injector.router()
            }
        }
    }
    private var binding: ActivityMainBinding? = null

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        return super.getDefaultViewModelCreationExtras()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ActivityMainBinding
                .inflate(layoutInflater, null, false)
                .also { binding = it }
                .root
        )

        // Collect the state and react to events during the Fragment's Lifecycle RESUMED state
        vm.runOnLifecycle(this, BallastExamplesRouterEventHandler(this)) { state ->
            binding?.updateWithState(state) { event -> vm.trySend(event) }
        }
    }

    private fun ActivityMainBinding.updateWithState(
        state: RouterContract.State<BallastExamples>,
        postInput: (RouterContract.Inputs<BallastExamples>) -> Unit
    ) {

    }
}
