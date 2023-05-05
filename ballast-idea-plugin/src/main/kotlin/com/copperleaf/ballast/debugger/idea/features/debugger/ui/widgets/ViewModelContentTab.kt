package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.ui.graphics.vector.ImageVector
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.versions.ClientVersion
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter

enum class ViewModelContentTab(
    val icon: ImageVector,
    val text: String,
) {
    States(Icons.Default.List, "States"),
    Inputs(Icons.Default.Refresh, "Inputs"),
    Events(Icons.Default.NotificationsActive, "Events"),
    SideJobs(Icons.Default.CloudUpload, "SideJobs"),
    Interceptors(Icons.Default.RestartAlt, "Interceptors"),
    Logs(Icons.Default.Description, "Logs");
//    Timeline(Icons.Default.Timeline, "Timeline");

    fun isEnabled(
        connection: BallastConnectionState
    ): Boolean {
        val version = ClientVersion.parse(connection.connectionBallastVersion)
        return when (this) {
            States -> true
            Inputs -> true
            Events -> true
            SideJobs -> true
            Interceptors -> version >= ClientVersion(3, null, null)
            Logs -> true
//            Timeline -> version >= ClientVersion(3, null, null)
        }
    }

    fun isActive(
        viewModel: BallastViewModelState,
    ): Boolean {
        return when (this) {
            States -> false
            Inputs -> viewModel.inputInProgress
            Events -> viewModel.eventInProgress
            SideJobs -> viewModel.sideJobsInProgress
            Interceptors -> false
            Logs -> false
//            Timeline -> false
        }
    }

    fun navigate(
        connection: BallastConnectionState,
        viewModel: BallastViewModelState,
    ): DebuggerUiContract.Inputs.Navigate {
        val route = when (this) {
            States -> DebuggerRoute.ViewModelStates.directions()
            Inputs -> DebuggerRoute.ViewModelInputs.directions()
            Events -> DebuggerRoute.ViewModelEvents.directions()
            SideJobs -> DebuggerRoute.ViewModelSideJobs.directions()
            Interceptors -> DebuggerRoute.ViewModelInterceptors.directions()
            Logs -> DebuggerRoute.ViewModelLogs.directions()
//            Timeline -> DebuggerRoute.ViewModelTimeline.directions()
        }

        return route
            .pathParameter("connectionId", connection.connectionId)
            .pathParameter("viewModelName", viewModel.viewModelName)
            .build()
            .let { DebuggerUiContract.Inputs.Navigate(it) }
    }

    companion object {
        fun fromRoute(route: DebuggerRoute): ViewModelContentTab? {
            return when (route) {
                DebuggerRoute.Home -> null
                DebuggerRoute.Connection -> null
                DebuggerRoute.ViewModelStates -> States
                DebuggerRoute.ViewModelStateDetails -> States
                DebuggerRoute.ViewModelInputs -> Inputs
                DebuggerRoute.ViewModelInputDetails -> Inputs
                DebuggerRoute.ViewModelEvents -> Events
                DebuggerRoute.ViewModelEventDetails -> Events
                DebuggerRoute.ViewModelSideJobs -> SideJobs
                DebuggerRoute.ViewModelSideJobDetails -> SideJobs
                DebuggerRoute.ViewModelInterceptors -> Interceptors
                DebuggerRoute.ViewModelInterceptorDetails -> Interceptors
                DebuggerRoute.ViewModelLogs -> Logs
//                DebuggerRoute.ViewModelTimeline -> Timeline
            }
        }
    }
}
