package com.copperleaf.ballast.debugger.ui.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.vector.ImageVector

enum class ViewModelContentTab(
    val icon: ImageVector,
    val text: String,
) {
    States(Icons.Default.List, "States"),
    Inputs(Icons.Default.Refresh, "Inputs"),
    Events(Icons.Default.NotificationsActive, "Events"),
    SideJobs(Icons.Default.CloudUpload, "SideJobs"),
}
