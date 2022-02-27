package com.copperleaf.ballast.debugger.idea.tool

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.debugger.idea.base.ComposeToolWindow
import com.copperleaf.ballast.debugger.ui.samplecontroller.SampleControllerUi

class BallastSampleToolWindow : ComposeToolWindow() {

    @Composable
    override fun Content() {
        SampleControllerUi.run()
    }
}
