@file:Suppress("UNCHECKED_CAST")

package com.copperleaf.ballast.ktor

import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

public val Ballast: ApplicationPlugin<BallastKtorPluginConfiguration> = createApplicationPlugin(
    name = "Ballast",
    createConfiguration = ::BallastKtorPluginConfiguration
) {
    // Standalone job not parented to the application scope. Ktor cancelling its
    // own coroutine scope on SIGTERM will not immediately cancel the VMs;
    // we control their lifecycle explicitly via graceful shutdown.
    val ballastJob = SupervisorJob()

    on(MonitoringEvent(ApplicationStarted)) { application ->
        // Replace the application's Job with our standalone one so VM coroutines
        // are children of ballastJob, not the application scope.
        val ballastScope = CoroutineScope(application.coroutineContext + ballastJob)
        pluginConfig.viewModels.forEach { vm ->
            vm.startProcessing(application, ballastScope)
        }
    }

    on(MonitoringEvent(ApplicationStopping)) { _ ->
        // MonitoringEvent handlers are synchronous; runBlocking bridges into
        // the coroutine world so we block until graceful shutdown completes
        // before returning and allowing Ktor to continue its shutdown sequence.
        runBlocking {
            pluginConfig.viewModels.forEach { vm ->
                vm.shutDownGracefully()
            }
        }
        ballastJob.cancel()
    }
}
