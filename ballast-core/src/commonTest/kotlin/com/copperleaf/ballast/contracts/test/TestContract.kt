package com.copperleaf.ballast.contracts.test

object TestContract {
    data class State(
        val acceptedFilteredValue: Boolean = false,
        val stringValue: String = "",
        val intValue: Int = 0,
    )

    sealed interface Inputs {
        data object FilteredValue : Inputs
        data object ThrowErrorDuringHandling : Inputs
        data class UpdateStringValue(val stringValue: String) : Inputs
        data object Increment : Inputs
        data object Decrement : Inputs
        data object IncrementWithRollback : Inputs
        data object MultipleStateUpdates : Inputs
        data object EventEmitted : Inputs
        data object SideJobStartedNoInputOverride : Inputs
        data object SideJobStartedWithInputOverride : Inputs
        data object MultipleSideJobs : Inputs
        data object SideJobsNotAtEnd : Inputs
        data object TestTimeout : Inputs
    }

    sealed interface Events {
        data object Notification : Events
    }
}
