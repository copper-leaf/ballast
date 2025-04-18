package com.copperleaf.ballast.debugger.idea.features.settings.vm

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class SettingsUiEventHandler : EventHandler<
        SettingsUiContract.Inputs,
        SettingsUiContract.Events,
        SettingsUiContract.State> {
    override suspend fun EventHandlerScope<
            SettingsUiContract.Inputs,
            SettingsUiContract.Events,
            SettingsUiContract.State>.handleEvent(
        event: SettingsUiContract.Events
    ) {
    }
}
