package com.copperleaf.ballast.debugger.idea.features.settings.ui

import com.copperleaf.ballast.debugger.idea.base.BallastComposePanel
import com.copperleaf.ballast.debugger.idea.features.settings.injector.SettingsPanelInjector
import com.copperleaf.ballast.debugger.idea.features.settings.vm.SettingsUiContract
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

/**
 * See https://plugins.jetbrains.com/docs/intellij/settings-guide.html#the-configurable-interface
 */
@Suppress("UnstableApiUsage")
class BallastPluginSettingsPanel(
    private val project: Project
) : Configurable, Configurable.NoScroll {
    private val injector = SettingsPanelInjector.getInstance(project)

    override fun getDisplayName(): String = "Ballast"

    override fun createComponent(): JComponent {
        return BallastComposePanel {
            SettingsUi.Content(injector)
        }
    }

    override fun isModified(): Boolean {
        return injector.settingsPanelViewModel.observeStates().value.isModified
    }

    override fun disposeUIResources() {
        injector.settingsPanelViewModel.trySend(SettingsUiContract.Inputs.CloseGracefully)
    }

    override fun reset() {
        injector.settingsPanelViewModel.trySend(SettingsUiContract.Inputs.DiscardChanges)
    }

    override fun apply() {
        injector.settingsPanelViewModel.trySend(SettingsUiContract.Inputs.ApplySettings)
    }
}
