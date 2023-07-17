package com.copperleaf.ballast.savedstate

public interface SavedStateAdapter<Inputs : Any, Events : Any, State : Any> {
    /**
     * Called when a new State is emitted that should be saved
     */
    public suspend fun SaveStateScope<Inputs, Events, State>.save()

    /**
     * Called when the ViewModel starts up and the state should be restored, or when a restoration is manually requested
     */
    public suspend fun RestoreStateScope<Inputs, Events, State>.restore(): State
}
