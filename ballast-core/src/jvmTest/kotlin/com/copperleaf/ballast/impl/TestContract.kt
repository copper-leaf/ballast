package com.copperleaf.ballast.impl

object TestContract {
    data class State(
        val acceptedFilteredValue: Boolean = false,
        val stringValue: String = "",
        val intValue: Int = 0,
    )

    sealed class Inputs {
        object FilteredValue : Inputs()
        object ThrowErrorDuringHandling : Inputs()
        data class UpdateStringValue(val stringValue: String) : Inputs()
        object Increment : Inputs()
        object Decrement : Inputs()
        object IncrementWithRollback : Inputs()
        object MultipleStateUpdates : Inputs()
        object EventEmitted : Inputs()
        object SideJobStartedNoInputOverride : Inputs()
        object SideJobStartedWithInputOverride : Inputs()
        object MultipleSideJobs : Inputs()
        object SideJobsNotAtEnd : Inputs()
        object TestTimeout : Inputs()
    }

    sealed class Events {
        object Notification : Events()
    }
}
