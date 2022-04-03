package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefsImpl.Companion.CONNECTIONS_DEFAULT_VALUE
import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefsImpl.Companion.EVENTS_DEFAULT_VALUE
import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefsImpl.Companion.VIEW_MODELS_DEFAULT_VALUE
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

object InMemoryIdeaPluginPrefs : IdeaPluginPrefs {
    override var connectionsPanePercentage: Float = CONNECTIONS_DEFAULT_VALUE
    override var viewModelsPanePercentage: Float = VIEW_MODELS_DEFAULT_VALUE
    override var eventsPanePercentage: Float = EVENTS_DEFAULT_VALUE
    override var selectedViewModelContentTab: ViewModelContentTab = ViewModelContentTab.Inputs
    override var sampleInputStrategy: InputStrategySelection = InputStrategySelection.Lifo
}

class IdeaPluginPrefsImpl(
    private val project: Project,
) : IdeaPluginPrefs {
    private val properties get() = PropertiesComponent.getInstance(project)

    companion object {
        private const val PREFIX = "BALLAST"

        const val CONNECTIONS_KEY = "$PREFIX.connectionsPanePercentage"
        const val CONNECTIONS_DEFAULT_VALUE = 0.30f

        const val VIEW_MODELS_KEY = "$PREFIX.viewModelsPanePercentage"
        const val VIEW_MODELS_DEFAULT_VALUE = 0.35f

        const val EVENTS_KEY = "$PREFIX.eventsPanePercentage"
        const val EVENTS_DEFAULT_VALUE = 0.45f

        const val SELECTED_VM_CONTENT_TAB_KEY = "$PREFIX.selectedViewModelContentTab"
        const val SAMPLE_INPUT_STRATEGY_KEY = "$PREFIX.sampleInputStrategy"
    }

    override var connectionsPanePercentage: Float
        get() = properties.getFloat(CONNECTIONS_KEY, CONNECTIONS_DEFAULT_VALUE)
        set(value) { properties.setValue(CONNECTIONS_KEY, value, CONNECTIONS_DEFAULT_VALUE) }

    override var viewModelsPanePercentage: Float
        get() = properties.getFloat(VIEW_MODELS_KEY, VIEW_MODELS_DEFAULT_VALUE)
        set(value) { properties.setValue(VIEW_MODELS_KEY, value, VIEW_MODELS_DEFAULT_VALUE) }

    override var eventsPanePercentage: Float
        get() = properties.getFloat(EVENTS_KEY, EVENTS_DEFAULT_VALUE)
        set(value) { properties.setValue(EVENTS_KEY, value, EVENTS_DEFAULT_VALUE) }

    override var selectedViewModelContentTab: ViewModelContentTab
        get() = properties.getValue(SELECTED_VM_CONTENT_TAB_KEY)
            ?.let { storedValue -> runCatching { ViewModelContentTab.valueOf(storedValue) }.getOrNull() }
            ?: ViewModelContentTab.Inputs
        set(value) { properties.setValue(SELECTED_VM_CONTENT_TAB_KEY, value.name) }

    override var sampleInputStrategy: InputStrategySelection
        get() = properties.getValue(SAMPLE_INPUT_STRATEGY_KEY)
            ?.let { storedValue -> runCatching { InputStrategySelection.valueOf(storedValue) }.getOrNull() }
            ?: InputStrategySelection.Lifo
        set(value) { properties.setValue(SAMPLE_INPUT_STRATEGY_KEY, value.name) }
}
