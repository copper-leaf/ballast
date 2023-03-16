package com.copperleaf.ballast.debugger.idea.features.settings.vm

import com.copperleaf.ballast.core.BasicViewModel

typealias SettingsUiViewModel = BasicViewModel<
        SettingsUiContract.Inputs,
        SettingsUiContract.Events,
        SettingsUiContract.State>
