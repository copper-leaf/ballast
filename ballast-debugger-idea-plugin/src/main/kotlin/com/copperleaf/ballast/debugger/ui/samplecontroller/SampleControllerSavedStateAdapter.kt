package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefs
import com.copperleaf.ballast.savedstate.PerformSaveStateScope
import com.copperleaf.ballast.savedstate.SavedStateAdapter

class SampleControllerSavedStateAdapter(
    private val prefs: IdeaPluginPrefs,
) : SavedStateAdapter<
    SampleControllerContract.Inputs,
    SampleControllerContract.Events,
    SampleControllerContract.State> {

    override suspend fun PerformSaveStateScope<
        SampleControllerContract.Inputs,
        SampleControllerContract.Events,
        SampleControllerContract.State>.save() {
        diff({ inputStrategy }) {
            prefs.sampleInputStrategy = it
        }
    }

    override suspend fun restore(): SampleControllerContract.State {
        return SampleControllerContract.State(
            sampleSourcesUrl = "blah",
            inputStrategy = prefs.sampleInputStrategy,
        )
    }

    override suspend fun onRestoreComplete(restoredState: SampleControllerContract.State): SampleControllerContract.Inputs {
        return SampleControllerContract.Inputs.Initialize
    }
}
