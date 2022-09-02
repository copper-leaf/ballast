package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.test.internal.vm.TestEventHandler
import com.copperleaf.ballast.test.internal.vm.TestInputFilter
import com.copperleaf.ballast.test.internal.vm.TestInputHandler
import com.copperleaf.ballast.test.internal.vm.TestInterceptor
import com.copperleaf.ballast.test.internal.vm.TestViewModel
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalCoroutinesApi
@ExperimentalTime
internal suspend fun <Inputs : Any, Events : Any, State : Any> runTestSuite(
    testSuite: BallastTestSuiteScopeImpl<Inputs, Events, State>,
) = supervisorScope {
    if(testSuite.skip) return@supervisorScope

    val totalTestTime = measureTime {
        val hasSoloScenario = testSuite.scenarioBlocks.any { it.solo }

        val actualScenariosToRun = if (hasSoloScenario) {
            testSuite.scenarioBlocks.filter { it.solo }
        } else {
            testSuite.scenarioBlocks
        }

        val results: List<ScenarioResult<Inputs, Events, State>> = actualScenariosToRun
            .map { scenario ->
                if (scenario.skip) {
                    CompletableDeferred(ScenarioResult.Skipped(scenario))
                } else {
                    async {
                        val result: Result<Unit>
                        val scenarioTestTime = measureTime {
                            result = runCatching { runScenario(testSuite, scenario) }
                        }
                        result.fold(
                            onSuccess = { ScenarioResult.Passed(scenario, scenarioTestTime) },
                            onFailure = { ScenarioResult.Failed(scenario, scenarioTestTime, it) },
                        )
                    }
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
private suspend fun <Inputs : Any, Events : Any, State : Any> runScenario(
    testSuite: BallastTestSuiteScopeImpl<Inputs, Events, State>,
    scenario: BallastScenarioScopeImpl<Inputs, Events, State>
) = supervisorScope {
    val scenarioLoggerFactory = scenario.logger ?: testSuite.suiteLogger
    val scenarioTimeout = scenario.timeout ?: testSuite.defaultTimeout
    val scenarioInputStrategy = scenario.inputStrategy ?: testSuite.inputStrategy
    val otherInterceptors = scenario.interceptors + testSuite.interceptors

    val testInterceptor = TestInterceptor<Inputs, Events, State>()

    val realConfig: BallastViewModelConfiguration<TestViewModel.Inputs<Inputs>, Events, State> = BallastViewModelConfiguration.Builder(scenario.name)
        .apply {
            this.logger = scenarioLoggerFactory
            this.inputStrategy = scenarioInputStrategy

            this += otherInterceptors.map { it() }
            this += testInterceptor
        }
        .withViewModel(
            initialState = scenario.givenBlock?.invoke()
                ?: testSuite.defaultInitialStateBlock?.invoke()
                ?: error("No initial state given"),
            inputHandler = TestInputHandler(testSuite.inputHandler),
            filter = testSuite.filter?.let { TestInputFilter(it) },
        )
        .build()

    val scenarioLogger = realConfig.logger

    scenarioLogger.debug("Scenario '${scenario.name}'")
    scenarioLogger.debug("before runScenario")
    val testViewModel = TestViewModel(
        impl = BallastViewModelImpl(realConfig),
    )

    // start running the VM in the background
    val viewModelJob = launch(start = CoroutineStart.UNDISPATCHED) {
        testViewModel.impl.start(this) { testViewModel }
    }
    val eventHandlerJob = launch(start = CoroutineStart.UNDISPATCHED) {
        testViewModel.impl.attachEventHandler(TestEventHandler(testSuite.eventHandler))
    }

    val inputSequenceScope = BallastScenarioInputSequenceScopeImpl(scenarioLogger, testViewModel)

    // run the scenario input sequence
    scenarioLogger.debug("    before onInputSequenceBlock")
    scenario.onInputSequenceBlock(inputSequenceScope)
    scenarioLogger.debug("    after onInputSequenceBlock")

    scenarioLogger.debug("    before awaitSideJobsCompletion")
    withTimeoutOrNull(scenarioTimeout) {
        testViewModel.impl.awaitSideJobsCompletion()
    }
    scenarioLogger.debug("    after awaitSideJobsCompletion")

    // await test completion
    scenarioLogger.debug("    before completing whole test")
    inputSequenceScope.finish()
    scenarioLogger.debug("    after completing whole test")

    // if the test passed, manually clear everything
    scenarioLogger.debug("    before cleanup")
    viewModelJob.cancelAndJoin()
    eventHandlerJob.cancelAndJoin()
    scenarioLogger.debug("    after cleanup")

    // make assertions on the VM. Errors should get captured and thrown by this coroutine scope, cancelling
    // everything if there are failures
    scenarioLogger.debug("    before verification")
    scenario.verifyBlock(testInterceptor.getResults())
    scenarioLogger.debug("    after verification")

    scenarioLogger.debug("after runScenario")
}
