package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.idea.features.templates.BallastViewModel

/**
 * Save the preferences for generating templates
 *
 * See https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html#implementing-the-persistentstatecomponent-interface
 */
interface TemplatesSettings {
    val baseViewModelType: BallastViewModel.ViewModelTemplate
    val allComponentsIncludesViewModel: Boolean
    val allComponentsIncludesSavedStateAdapter: Boolean
    val allComponentsIncludesComposeUi: Boolean
    val defaultVisibility: BallastViewModel.DefaultVisibility
    val useDataObjects: Boolean
}

