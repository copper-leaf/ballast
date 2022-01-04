package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.test.ViewModelTestScenarioScope
import com.copperleaf.ballast.test.ViewModelTestSuiteScope
import com.copperleaf.ballast.test.internal.vm.TestEventHandler
import com.copperleaf.ballast.test.internal.vm.TestInputFilter
import com.copperleaf.ballast.test.internal.vm.TestInterceptor
import com.copperleaf.ballast.test.internal.vm.TestViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalCoroutinesApi
@ExperimentalTime
internal class ViewModelTestSuiteScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val inputHandler: InputHandler<Inputs, Events, State>,
    private val eventHandler: EventHandler<Inputs, Events, State>,
    private val filter: InputFilter<Inputs, Events, State>?,
) : ViewModelTestSuiteScope<Inputs, Events, State> {

    private var suiteLogger: (String) -> Unit = { }
    private var defaultTimeout: Duration = Duration.seconds(30)

    private var defaultInitialStateBlock: (() -> State)? = null
    private val scenarioBlocks = mutableListOf<ViewModelTestScenarioScopeImpl<Inputs, Events, State>>()

    override fun logger(block: (String) -> Unit) {
        suiteLogger = block
    }

    override fun defaultTimeout(timeout: Duration) {
        defaultTimeout = timeout
    }

    override fun defaultInitialState(block: () -> State) {
        defaultInitialStateBlock = block
    }

    override fun scenario(name: String, block: ViewModelTestScenarioScope<Inputs, Events, State>.() -> Unit) {
        scenarioBlocks += ViewModelTestScenarioScopeImpl<Inputs, Events, State>(name).apply(block)
    }

    private suspend fun runScenario(scenario: ViewModelTestScenarioScopeImpl<Inputs, Events, State>) = supervisorScope {
        val scenarioLogger = scenario.logger ?: suiteLogger
        val scenarioTimeout = scenario.timeout ?: defaultTimeout

        scenarioLogger("Scenario '${scenario.name}'")
        scenarioLogger("before runScenario")
        val testViewModel = TestViewModel(
            logger = scenarioLogger,
            interceptor = TestInterceptor(),
            initialState = scenario.givenBlock?.invoke()
                ?: defaultInitialStateBlock?.invoke()
                ?: error("No initial state given"),
            inputHandler = inputHandler,
            filter = filter?.let { TestInputFilter(it) },
        )

        // start running the VM in the background
        val viewModelJob = launch(start = CoroutineStart.UNDISPATCHED) {
            testViewModel.impl.start(this)
        }
        val eventHandlerJob = launch(start = CoroutineStart.UNDISPATCHED) {
            testViewModel.impl.attachEventHandler(TestEventHandler(eventHandler))
        }

        val inputSequenceScope = ViewModelTestScenarioInputSequenceScopeImpl(scenarioLogger, testViewModel)

        // run the scenario input sequence
        scenarioLogger("    before onInputSequenceBlock")
        scenario.onInputSequenceBlock(inputSequenceScope)
        scenarioLogger("    after onInputSequenceBlock")

        scenarioLogger("    before awaitSideEffectsCompletion")
        withTimeoutOrNull(scenarioTimeout) {
            testViewModel.impl.awaitSideEffectsCompletion()
        }
        scenarioLogger("    after awaitSideEffectsCompletion")

        // await test completion
        scenarioLogger("    before completing whole test")
        inputSequenceScope.finish()
        scenarioLogger("    after completing whole test")

        // if the test passed, manually clear everything
        scenarioLogger("    before cleanup")
        viewModelJob.cancelAndJoin()
        eventHandlerJob.cancelAndJoin()
        testViewModel.onCleared()
        scenarioLogger("    after cleanup")

        // make assertions on the VM. Errors should get captured and thrown by this coroutine scope, cancelling
        // everything if there are failures
        scenarioLogger("    before verification")
        scenario.verifyBlock(testViewModel.interceptor.getResults())
        scenarioLogger("    after verification")

        scenarioLogger("after runScenario")
    }

    private sealed class ScenarioResult<Inputs : Any, Events : Any, State : Any> {
        abstract val scenario: ViewModelTestScenarioScopeImpl<Inputs, Events, State>
        abstract fun printResults(): String

        data class Passed<Inputs : Any, Events : Any, State : Any>(
            override val scenario: ViewModelTestScenarioScopeImpl<Inputs, Events, State>,
            val time: Duration
        ) : ScenarioResult<Inputs, Events, State>() {
            override fun printResults(): String {
                return "Scenario '${scenario.name}': Passed ($time)"
            }
        }

        data class Failed<Inputs : Any, Events : Any, State : Any>(
            override val scenario: ViewModelTestScenarioScopeImpl<Inputs, Events, State>,
            val time: Duration,
            val reason: Throwable,
        ) : ScenarioResult<Inputs, Events, State>() {
            override fun printResults(): String {
                return "Scenario '${scenario.name}': Failed ($time)"
            }
        }
    }

    internal suspend fun runTest() = supervisorScope {
        val totalTestTime = measureTime {
            val results: List<ScenarioResult<Inputs, Events, State>> = scenarioBlocks.map { scenario ->
                async {
                    val result: Result<Unit>
                    val scenarioTestTime = measureTime {
                        result = runCatching { runScenario(scenario) }
                    }
                    result.fold(
                        onSuccess = { ScenarioResult.Passed(scenario, scenarioTestTime) },
                        onFailure = { ScenarioResult.Failed(scenario, scenarioTestTime, it) },
                    )
                }
            }.awaitAll()

            results.forEach {
                val scenarioLogger = it.scenario.logger ?: suiteLogger
                scenarioLogger(it.printResults())
            }

            results.filterIsInstance<ScenarioResult.Failed<*, *, *>>().firstOrNull()?.let {
                throw it.reason
            }
        }

        suiteLogger("All scenarios completed in $totalTestTime")
    }
}
