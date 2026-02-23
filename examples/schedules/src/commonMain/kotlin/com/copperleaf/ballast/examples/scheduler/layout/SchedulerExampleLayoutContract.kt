package com.copperleaf.ballast.examples.scheduler.layout

object SchedulerExampleLayoutContract {
    data class State(
        val tab: LayoutTabs = LayoutTabs.Persistent,
    )

    sealed interface Inputs {
        data class ChangeTab(val tab: LayoutTabs) : Inputs
    }

    sealed interface Events {
        object NavigateUp : Events
    }
}
