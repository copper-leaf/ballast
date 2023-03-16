package com.copperleaf.ballast.debugger.idea

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.debugger.idea.repository.RepositoryViewModel
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

interface BallastIntellijPluginInjector {
    val project: Project

    val mainCoroutineDispatcher: CoroutineDispatcher
    val defaultCoroutineDispatcher: CoroutineDispatcher
    val ioCoroutineDispatcher: CoroutineDispatcher

    fun commonViewModelBuilder(
        loggingEnabled: Boolean,
        bootstrapInput: (() -> Any)? = null,
    ): BallastViewModelConfiguration.Builder

    fun newMainCoroutineScope(): CoroutineScope

    val repository: RepositoryViewModel

    companion object {
        private val projectMap = mutableMapOf<Project, BallastIntellijPluginInjector>()
        fun getInstance(project: Project): BallastIntellijPluginInjector {
            return projectMap.computeIfAbsent(project) {
                BallastIntellijPluginInjectorImpl(it)
            }
        }
    }
}
