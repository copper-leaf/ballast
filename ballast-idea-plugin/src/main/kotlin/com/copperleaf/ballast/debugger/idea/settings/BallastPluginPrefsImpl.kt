package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.base.BasePluginPrefs
import com.copperleaf.ballast.debugger.ui.widgets.ViewModelContentTab
import com.intellij.openapi.project.Project

class BallastPluginPrefsImpl(
    project: Project,
) : BasePluginPrefs(project, prefix = "BALLAST") {
    companion object {
        const val CONNECTIONS_DEFAULT_VALUE = 0.30f
        const val VIEW_MODELS_DEFAULT_VALUE = 0.35f
        const val EVENTS_DEFAULT_VALUE = 0.45f
    }

    override var debuggerPort: Int by int(9684)
    override var connectionsPanePercentage: Float by float(CONNECTIONS_DEFAULT_VALUE)
    override var viewModelsPanePercentage: Float by float(VIEW_MODELS_DEFAULT_VALUE)
    override var eventsPanePercentage: Float by float(EVENTS_DEFAULT_VALUE)
    override var selectedViewModelContentTab: ViewModelContentTab by enum(ViewModelContentTab.Inputs, ViewModelContentTab::valueOf)
}
