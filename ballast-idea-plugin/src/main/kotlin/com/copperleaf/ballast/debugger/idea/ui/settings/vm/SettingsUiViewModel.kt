package com.copperleaf.ballast.debugger.idea.ui.settings.vm

import com.copperleaf.ballast.core.BasicViewModel

typealias SettingsUiViewModel = BasicViewModel<
        SettingsUiContract.Inputs,
        SettingsUiContract.Events,
        SettingsUiContract.State>
