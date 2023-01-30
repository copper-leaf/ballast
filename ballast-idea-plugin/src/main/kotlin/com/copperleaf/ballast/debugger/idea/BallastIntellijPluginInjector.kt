package com.copperleaf.ballast.debugger.idea

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginPersistentSettings
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

interface BallastIntellijPluginInjector {
    val project: Project
    val settings: IntellijPluginPersistentSettings

    val mainCoroutineDispatcher: CoroutineDispatcher
    val defaultCoroutineDispatcher: CoroutineDispatcher
    val ioCoroutineDispatcher: CoroutineDispatcher

    fun commonViewModelBuilder(): BallastViewModelConfiguration.Builder
    fun newMainCoroutineScope(): CoroutineScope

    companion object {
        fun getInstance(project: Project): BallastIntellijPluginInjector {
            return BallastIntellijPluginInjectorImpl(project)
        }
    }
}
