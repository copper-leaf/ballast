package com.copperleaf.ballast.debugger.di

import androidx.compose.runtime.compositionLocalOf
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.debugger.idea.settings.BallastPluginPrefs
import com.copperleaf.ballast.debugger.idea.settings.BallastPluginPrefsImpl
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerEventHandler
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerInputHandler
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerSavedStateAdapter
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerViewModel
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

val LocalProject = compositionLocalOf<Project> { error("LocalProject not provided") }
val LocalInjector = compositionLocalOf<BallastDebuggerInjector> { error("LocalInjector not provided") }

interface BallastDebuggerInjector {
    val prefs: BallastPluginPrefs

    fun debuggerViewModel(
        coroutineScope: CoroutineScope,
    ): DebuggerViewModel

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
    override val prefs: BallastPluginPrefs = BallastPluginPrefsImpl(project)

    override fun debuggerViewModel(coroutineScope: CoroutineScope): DebuggerViewModel {
        return DebuggerViewModel(
            coroutineScope = coroutineScope,
            configurationBuilder = BallastViewModelConfiguration.Builder(),
            inputHandler = DebuggerInputHandler(),
            eventHandler = DebuggerEventHandler(),
            savedStateAdapter = DebuggerSavedStateAdapter(prefs)
        )
    }
}

