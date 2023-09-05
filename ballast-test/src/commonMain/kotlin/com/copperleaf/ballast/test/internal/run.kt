package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalCoroutinesApi
@ExperimentalTime
internal suspend fun <Inputs : Any, Events : Any, State : Any> runTestSuite(
    testSuite: BallastTestSuiteScopeImpl<Inputs, Events, State>,
) {
    runTestSuiteInParallel(testSuite)
//    runTestSuiteInSeries(testSuite)
}

@ExperimentalCoroutinesApi
@ExperimentalTime
internal suspend fun <Inputs : Any, Events : Any, State : Any> runTestSuiteInParallel(
    testSuite: BallastTestSuiteScopeImpl<Inputs, Events, State>,
) = supervisorScope {
    if (testSuite.skip) return@supervisorScope

    val totalTestTime = measureTime {
        val hasSoloScenario = testSuite.scenarioBlocks.any { it.solo }

        val actualScenariosToRun = if (hasSoloScenario) {
            testSuite.scenarioBlocks.filter { it.solo }
        } else {
            testSuite.scenarioBlocks
        }

        val results: List<ScenarioResult<Inputs, Events, State>> = actualScenariosToRun
            .map { scenario ->
                async {
                    runScenario(testSuite, scenario)
                }
            }
            .awaitAll()

        results.forEach {
            testSuite.suiteLogger("").info(it.printResults())
        }

        results.filterIsInstance<ScenarioResult.Failed<*, *, *>>().firstOrNull()?.let {
            throw it.reason
        }
    }

    testSuite.suiteLogger("").info("All scenarios completed in $totalTestTime")
}

@ExperimentalCoroutinesApi
@ExperimentalTime
internal suspend fun <Inputs : Any, Events : Any, State : Any> runTestSuiteInSeries(
    testSuite: BallastTestSuiteScopeImpl<Inputs, Events, State>,
) = supervisorScope {
    if (testSuite.skip) return@supervisorScope

    val totalTestTime = measureTime {
        val hasSoloScenario = testSuite.scenarioBlocks.any { it.solo }

        val actualScenariosToRun = if (hasSoloScenario) {
            testSuite.scenarioBlocks.filter { it.solo }
        } else {
            testSuite.scenarioBlocks
        }

        val results: List<ScenarioResult<Inputs, Events, State>> = actualScenariosToRun
            .map { scenario ->
                testSuite.suiteLogger("").info("Scenario '${scenario.name}'")
                val scenarioResult = runScenario(testSuite, scenario)
                testSuite.suiteLogger("").info(scenarioResult.printResults())
                scenarioResult
            }

        results.filterIsInstance<ScenarioResult.Failed<*, *, *>>().firstOrNull()?.let {
            throw it.reason
        }
    }

    testSuite.suiteLogger("").info("All scenarios completed in $totalTestTime")
}

@ExperimentalTime
@ExperimentalCoroutinesApi
private suspend fun <Inputs : Any, Events : Any, State : Any> runScenario(
    testSuite: BallastTestSuiteScopeImpl<Inputs, Events, State>,
    scenario: BallastScenarioScopeImpl<Inputs, Events, State>
): ScenarioResult<Inputs, Events, State> {
    if (scenario.skip) {
        return ScenarioResult.Skipped(scenario)
    }

    val onTestCompleted = CompletableDeferred<ScenarioResult<Inputs, Events, State>>()
    try {
        coroutineScope {
            BasicViewModel(
                config = BallastViewModelConfiguration
                    .Builder(scenario.name)
                    .apply {
                        this.logger = scenario.logger ?: testSuite.suiteLogger
                        this.inputStrategy = scenario.inputStrategy() ?: testSuite.inputStrategy()

                        this += (scenario.interceptors + testSuite.interceptors).map { it() }
                        this += TestInterceptor(
                            testCoroutineScope = this@coroutineScope,
                            onTestComplete = onTestCompleted,
                            scenario = scenario,
                            testSequenceTimeout = scenario.timeout ?: testSuite.defaultTimeout,
                        )
                    }
                    .withViewModel(
                        initialState = scenario.givenBlock?.invoke()
                            ?: testSuite.defaultInitialStateBlock?.invoke()
                            ?: error("No initial state given"),
                        inputHandler = testSuite.inputHandler,
                    )
                    .let {
                        scenario.configurationBlock?.invoke(it) ?: it
                    }
                    .build(),
                eventHandler = testSuite.eventHandler,
                coroutineScope = this
            )
        }
    } catch (e: CancellationException) {
        // ignore
    } catch (e: Throwable) {
        // a non-cancellation error, bubble it up
        throw e
    }

    return onTestCompleted.await()
}
