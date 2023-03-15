package com.copperleaf.ballast.debugger.idea

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.idea.base.IntellijPluginBallastLogger
import com.copperleaf.ballast.debugger.idea.repository.RepositoryContract
import com.copperleaf.ballast.debugger.idea.repository.RepositoryEventHandler
import com.copperleaf.ballast.debugger.idea.repository.RepositoryInputHandler
import com.copperleaf.ballast.debugger.idea.repository.RepositoryViewModel
import com.copperleaf.ballast.dispatchers
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.withViewModel
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.swing.Swing

class BallastIntellijPluginInjectorImpl(
    override val project: Project,
) : BallastIntellijPluginInjector {
    override val mainCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Swing
    override val defaultCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
    override val ioCoroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun newMainCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + defaultCoroutineDispatcher)
    }

    override fun commonViewModelBuilder(
        loggingEnabled: Boolean,
        bootstrapInput: (() -> Any)?
    ): BallastViewModelConfiguration.Builder {
        return BallastViewModelConfiguration.Builder()
            .apply {
                logger = { tag -> IntellijPluginBallastLogger(tag) }
                inputStrategy = FifoInputStrategy()

                if (loggingEnabled) {
                    this += LoggingInterceptor()
                }
                if (bootstrapInput != null) {
                    this += BootstrapInterceptor {
                        bootstrapInput()
                    }
                }
            }
            .dispatchers(
                inputsDispatcher = mainCoroutineDispatcher,
                eventsDispatcher = mainCoroutineDispatcher,
                sideJobsDispatcher = ioCoroutineDispatcher,
                interceptorDispatcher = defaultCoroutineDispatcher,
            )
    }

    override val repository: RepositoryViewModel = BasicViewModel(
        coroutineScope = newMainCoroutineScope(),
        config = commonViewModelBuilder(loggingEnabled = false) {
            RepositoryContract.Inputs.Initialize
        }
            .withViewModel(
                initialState = RepositoryContract.State(),
                inputHandler = RepositoryInputHandler(),
                name = "Repository",
            )
            .build(),
        eventHandler = RepositoryEventHandler(),
    )
}
