package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.debugger.idea.settings.IdeaPluginPrefs
import com.copperleaf.ballast.savedstate.RestoreStateScope
import com.copperleaf.ballast.savedstate.SaveStateScope
import com.copperleaf.ballast.savedstate.SavedStateAdapter

class SampleControllerSavedStateAdapter(
    private val prefs: IdeaPluginPrefs,
) : SavedStateAdapter<
    SampleControllerContract.Inputs,
    SampleControllerContract.Events,
    SampleControllerContract.State> {

    override suspend fun SaveStateScope<
        SampleControllerContract.Inputs,
        SampleControllerContract.Events,
        SampleControllerContract.State>.save() {
        saveDiff({ inputStrategy }) {
            prefs.sampleInputStrategy = it
        }
    }

    override suspend fun RestoreStateScope<
        SampleControllerContract.Inputs,
        SampleControllerContract.Events,
        SampleControllerContract.State>.restore(): SampleControllerContract.State {
        return SampleControllerContract.State(
            sampleSourcesUrl = "blah",
            inputStrategy = prefs.sampleInputStrategy,
        )
    }

    override suspend fun onRestoreComplete(restoredState: SampleControllerContract.State): SampleControllerContract.Inputs {
        return SampleControllerContract.Inputs.Initialize
    }

}
