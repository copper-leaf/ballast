package com.copperleaf.ballast.examples.presentation.ui

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.core.PrintlnLogger
import com.copperleaf.ballast.examples.di.ComposeDesktopInjector
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

class MainScreenViewModel(
    coroutineScope: CoroutineScope,
    injector: ComposeDesktopInjector,
) : BasicViewModel<
        MainScreenContract.Inputs,
        MainScreenContract.Events,
        MainScreenContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            initialState = MainScreenContract.State(),
            inputHandler = injector.mainScreenInputHandler(),
            name = "MainScreenViewModel",
        )
        .apply {
            interceptors += BootstrapInterceptor {
                MainScreenContract.Inputs.Initialize
            }
            interceptors += LoggingInterceptor()
            logger = ::PrintlnLogger
        }
        .build(),
    eventHandler = injector.mainScreenEventHandler(),
)
