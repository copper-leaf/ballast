package com.copperleaf.ballast.debugger.di

import androidx.compose.runtime.compositionLocalOf
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.debugger.idea.BallastIdeaPlugin
import com.copperleaf.ballast.debugger.idea.IntellijPluginBallastLogger
import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefs
import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefsImpl
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerEventHandler
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerInputHandler
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerViewModel
import com.copperleaf.ballast.debugger.ui.samplecontroller.SampleControllerEventHandler
import com.copperleaf.ballast.debugger.ui.samplecontroller.SampleControllerInputHandler
import com.copperleaf.ballast.debugger.ui.samplecontroller.SampleControllerViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkViewModel
import com.copperleaf.ballast.plusAssign
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val LocalInjector = compositionLocalOf<BallastDebuggerInjector> { error("LocalInjector not provided") }

interface BallastDebuggerInjector {
    fun debuggerViewModel(
        coroutineScope: CoroutineScope,
    ): DebuggerViewModel

    fun sampleControllerViewModel(
        coroutineScope: CoroutineScope,
    ): SampleControllerViewModel

    fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy,
    ): KitchenSinkViewModel

    companion object {
        private val injectors = mutableMapOf<Project, BallastDebuggerInjector>()

        fun getInstance(project: Project): BallastDebuggerInjector {
            return injectors.getOrPut(project) { BallastDebuggerInjectorImpl(project) }
        }
    }
}

class BallastDebuggerInjectorImpl(
    private val project: Project,
) : BallastDebuggerInjector {
    private val ideaPluginLogger: Logger = Logger.getInstance(BallastIdeaPlugin::class.java)
    private val prefs: IdeaPluginPrefs = IdeaPluginPrefsImpl(project)
    private val toolWindowManager: ToolWindowManager get() = ToolWindowManager.getInstance(project)
    private val uncaughtExceptionHandler = CoroutineExceptionHandler { _, _ ->
        // ignore
    }
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + uncaughtExceptionHandler)
    private val debuggerConnection by lazy {
        BallastDebuggerClientConnection(CIO, applicationScope).also { it.connect() }
    }

    private fun commonBuilder(): BallastViewModelConfiguration.Builder {
        return BallastViewModelConfiguration.Builder()
            .apply {
                logger = { IntellijPluginBallastLogger(ideaPluginLogger) }
            }
    }

    override fun debuggerViewModel(coroutineScope: CoroutineScope): DebuggerViewModel {
        return DebuggerViewModel(
            coroutineScope = coroutineScope,
            configurationBuilder = commonBuilder(),
            inputHandler = DebuggerInputHandler(prefs),
            eventHandler = DebuggerEventHandler(),
        )
    }

    override fun sampleControllerViewModel(coroutineScope: CoroutineScope): SampleControllerViewModel {
        return SampleControllerViewModel(
            coroutineScope = coroutineScope,
            configurationBuilder = commonBuilder(),
            inputHandler = SampleControllerInputHandler(this, prefs),
            eventHandler = SampleControllerEventHandler(),
        )
    }

    override fun kitchenSinkViewModel(
        coroutineScope: CoroutineScope,
        inputStrategy: InputStrategy,
    ): KitchenSinkViewModel {
        return KitchenSinkViewModel(
            viewModelCoroutineScope = coroutineScope,
            configurationBuilder = commonBuilder()
                .apply {
                    this.inputStrategy = inputStrategy
                    this += BallastDebuggerInterceptor(debuggerConnection)
                },
            onWindowClosed = {
                toolWindowManager.getToolWindow("Ballast Sample")?.hide()
            }
        )
    }
}

