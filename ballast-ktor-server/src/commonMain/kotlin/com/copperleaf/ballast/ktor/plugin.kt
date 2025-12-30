@file:Suppress("UNCHECKED_CAST")

package com.copperleaf.ballast.ktor

import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

public val Ballast: ApplicationPlugin<BallastKtorPluginConfiguration> = createApplicationPlugin(
    name = "Ballast",
    createConfiguration = ::BallastKtorPluginConfiguration
) {
    on(MonitoringEvent(ApplicationStarted)) { application ->
        application.launch {
            supervisorScope {
                pluginConfig.viewModels.forEach { vm ->
                    vm.startProcessing(application, this)
                }
            }
        }
    }
    on(MonitoringEvent(ApplicationStopping)) { application ->
        application.launch {
            supervisorScope {
                pluginConfig.viewModels.forEach { vm ->
                    vm.shutDownGracefully()
                }
            }
        }
    }
}
