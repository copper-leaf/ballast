package com.copperleaf.ballast.analytics.vm

object TestContract {
    data class State(
        val loading: Boolean = false,
    )

    sealed interface Inputs {
        data object TrackThis : Inputs
        data object DontTrackThis : Inputs
    }

    sealed interface Events
}
