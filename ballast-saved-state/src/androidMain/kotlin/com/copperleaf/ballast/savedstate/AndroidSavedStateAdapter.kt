package com.copperleaf.ballast.savedstate

import androidx.lifecycle.SavedStateHandle

public interface AndroidSavedStateAdapter<Inputs : Any, Events : Any, State : Any> : SavedStateAdapter<Inputs, Events, State> {

    public abstract val savedStateHandle: SavedStateHandle
    public open val prefix: (hostViewModelName: String) -> String get() = { it }

    public suspend fun <Prop> SaveStateScope<Inputs, Events, State>.saveDiffToSavedStateHandle(
        key: String,
        computeProperty: State.() -> Prop,
    ) {
        saveDiff(computeProperty) {
            savedStateHandle.set("${prefix(hostViewModelName)}.$key", it)
        }
    }

    public suspend fun SaveStateScope<Inputs, Events, State>.saveAllToSavedStateHandle() {
        saveAll {
            savedStateHandle.set(prefix(hostViewModelName), it)
        }
    }

    public fun <Prop> RestoreStateScope<Inputs, Events, State>.get(key: String, defaultValue: () -> Prop): Prop {
        return savedStateHandle.get<Prop>("${prefix(hostViewModelName)}.$key") ?: defaultValue()
    }
}
