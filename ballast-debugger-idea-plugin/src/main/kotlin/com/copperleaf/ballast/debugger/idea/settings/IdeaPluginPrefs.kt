package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.ui.samplecontroller.InputStrategySelection
import com.copperleaf.ballast.debugger.ui.widgets.ViewModelContentTab
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

/**
 * Save the UI state
 *   - Divider percentages
 *   - Selected VM tab
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
interface IdeaPluginPrefs {
    var connectionsPanePercentage: Float
    var viewModelsPanePercentage: Float
    var eventsPanePercentage: Float
    var selectedViewModelContentTab: ViewModelContentTab
    var sampleInputStrategy: InputStrategySelection
}

class IdeaPluginPrefsImpl(
    private val project: Project,
) : IdeaPluginPrefs {
    private val properties = PropertiesComponent.getInstance(project)
    private val prefix = this::class.java.name

    override var connectionsPanePercentage: Float
        get() {
            return properties.getFloat("$prefix.connectionsPanePercentage", 0.30f)
        }
        set(value) {
            properties.setValue("$prefix.connectionsPanePercentage", value, 0.30f)
        }
    override var viewModelsPanePercentage: Float
        get() {
            return properties.getFloat("$prefix.viewModelsPanePercentage", 0.35f)
        }
        set(value) {
            properties.setValue("$prefix.viewModelsPanePercentage", value, 0.35f)
        }
    override var eventsPanePercentage: Float
        get() {
            return properties.getFloat("$prefix.eventsPanePercentage", 0.45f)
        }
        set(value) {
            properties.setValue("$prefix.eventsPanePercentage", value, 0.45f)
        }
    override var selectedViewModelContentTab: ViewModelContentTab
        get() {
            return properties.getValue("$prefix.selectedViewModelContentTab")
                ?.let { storedValue -> runCatching { ViewModelContentTab.valueOf(storedValue) }.getOrNull() }
                ?: ViewModelContentTab.Inputs
        }
        set(value) {
            properties.setValue("$prefix.selectedViewModelContentTab", value.name)
        }

    override var sampleInputStrategy: InputStrategySelection
        get() {
            return properties.getValue("$prefix.sampleInputStrategy")
                ?.let { storedValue -> runCatching { InputStrategySelection.valueOf(storedValue) }.getOrNull() }
                ?: InputStrategySelection.Lifo
        }
        set(value) {
            properties.setValue("$prefix.sampleInputStrategy", value.name)
        }
}
