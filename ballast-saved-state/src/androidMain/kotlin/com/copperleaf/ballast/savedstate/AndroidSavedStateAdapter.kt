package com.copperleaf.ballast.savedstate

import androidx.lifecycle.SavedStateHandle

public interface AndroidSavedStateAdapter<Inputs : Any, Events : Any, State : Any> : SavedStateAdapter<Inputs, Events, State> {

    abstract val savedStateHandle: SavedStateHandle
    open val prefix: (hostViewModelName: String) -> String get() = { it }

    suspend fun <Prop> PerformSaveStateScope<Inputs, Events, State>.saveDiffToSavedStateHandle(
        key: String,
        computeProperty: State.() -> Prop,
    ) {
        saveDiff(computeProperty) {
            savedStateHandle.set("${prefix(hostViewModelName)}.$key", it)
        }
    }

    suspend fun PerformSaveStateScope<Inputs, Events, State>.saveAllToSavedStateHandle() {
        saveAll {
            savedStateHandle.set(prefix(hostViewModelName), it)
        }
    }

    fun <Prop> PerformRestoreStateScope<Inputs, Events, State>.get(key: String, defaultValue: () -> Prop): Prop {
        return savedStateHandle.get<Prop>("${prefix(hostViewModelName)}.$key") ?: defaultValue()
    }
}
