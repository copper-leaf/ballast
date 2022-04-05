package com.copperleaf.ballast.test.internal

import kotlin.time.Duration

internal sealed class ScenarioResult<Inputs : Any, Events : Any, State : Any> {
    abstract val scenario: BallastScenarioScopeImpl<Inputs, Events, State>
    abstract fun printResults(): String

    data class Passed<Inputs : Any, Events : Any, State : Any>(
        override val scenario: BallastScenarioScopeImpl<Inputs, Events, State>,
        val time: Duration
    ) : ScenarioResult<Inputs, Events, State>() {
        override fun printResults(): String {
            return "Scenario '${scenario.name}': Passed ($time)"
        }
    }

    data class Failed<Inputs : Any, Events : Any, State : Any>(
        override val scenario: BallastScenarioScopeImpl<Inputs, Events, State>,
        val time: Duration,
        val reason: Throwable,
    ) : ScenarioResult<Inputs, Events, State>() {
        override fun printResults(): String {
            return "Scenario '${scenario.name}': Failed ($time)"
        }
    }

    data class Skipped<Inputs : Any, Events : Any, State : Any>(
        override val scenario: BallastScenarioScopeImpl<Inputs, Events, State>,
    ) : ScenarioResult<Inputs, Events, State>() {
        override fun printResults(): String {
            return "Scenario '${scenario.name}': Skipped"
        }
    }
}
