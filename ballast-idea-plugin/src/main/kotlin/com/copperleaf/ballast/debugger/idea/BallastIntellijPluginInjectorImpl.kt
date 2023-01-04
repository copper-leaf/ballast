package com.copperleaf.ballast.debugger.idea

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.idea.base.IntellijPluginBallastLogger
import com.copperleaf.ballast.debugger.idea.settings.BallastIntellijPluginPersistentSettings
import com.copperleaf.ballast.dispatchers
import com.copperleaf.ballast.plusAssign
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.swing.Swing

class BallastIntellijPluginInjectorImpl(
    override val project: Project,
) : BallastIntellijPluginInjector {
    override val settings get() = BallastIntellijPluginPersistentSettings

    override val mainCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Swing
    override val defaultCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
    override val ioCoroutineDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun newMainCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + defaultCoroutineDispatcher)
    }

    override fun commonViewModelBuilder(): BallastViewModelConfiguration.Builder {
        return BallastViewModelConfiguration.Builder()
            .apply {
                this += LoggingInterceptor()
                logger = { tag -> IntellijPluginBallastLogger(Logger.getInstance(tag)) }
                inputStrategy = FifoInputStrategy()
            }
            .dispatchers(
                inputsDispatcher = mainCoroutineDispatcher,
                eventsDispatcher = mainCoroutineDispatcher,
                sideJobsDispatcher = ioCoroutineDispatcher,
                interceptorDispatcher = defaultCoroutineDispatcher,
            )
    }
}
