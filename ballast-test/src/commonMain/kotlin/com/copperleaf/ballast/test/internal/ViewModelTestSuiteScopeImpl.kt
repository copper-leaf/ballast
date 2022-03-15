package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.test.ViewModelTestScenarioScope
import com.copperleaf.ballast.test.ViewModelTestSuiteScope
import com.copperleaf.ballast.test.internal.vm.TestEventHandler
import com.copperleaf.ballast.test.internal.vm.TestInputFilter
import com.copperleaf.ballast.test.internal.vm.TestInterceptor
import com.copperleaf.ballast.test.internal.vm.TestInterceptorWrapper
import com.copperleaf.ballast.test.internal.vm.TestViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
internal class ViewModelTestSuiteScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val inputHandler: InputHandler<Inputs, Events, State>,
    private val eventHandler: EventHandler<Inputs, Events, State>,
    private val filter: InputFilter<Inputs, Events, State>?,
) : ViewModelTestSuiteScope<Inputs, Events, State> {

    private var suiteLogger: BallastLogger = SimpleTestLogger()
    private var defaultTimeout: Duration = 30.seconds
    private var inputStrategy: InputStrategy = LifoInputStrategy()

    internal val interceptors: MutableList<() -> BallastInterceptor<TestViewModel.Inputs<Inputs>, Events, State>> =
        mutableListOf()

    private var defaultInitialStateBlock: (() -> State)? = null
    private val scenarioBlocks = mutableListOf<ViewModelTestScenarioScopeImpl<Inputs, Events, State>>()

    override fun logger(logger: BallastLogger) {
        this.suiteLogger = logger
    }

    override fun defaultTimeout(timeout: () -> Duration) {
        this.defaultTimeout = timeout()
    }

    override fun addInterceptor(interceptor: () -> BallastInterceptor<Inputs, Events, State>) {
        this.interceptors += { TestInterceptorWrapper(interceptor()) }
    }

    override fun defaultInputStrategy(inputStrategy: () -> InputStrategy) {
        this.inputStrategy = inputStrategy()
    }

    override fun defaultInitialState(block: () -> State) {
        defaultInitialStateBlock = block
    }

    override fun scenario(name: String, block: ViewModelTestScenarioScope<Inputs, Events, State>.() -> Unit) {
        scenarioBlocks += ViewModelTestScenarioScopeImpl<Inputs, Events, State>(name).apply(block)
    }

    @ExperimentalCoroutinesApi
    private suspend fun runScenario(
        scenario: ViewModelTestScenarioScopeImpl<Inputs, Events, State>
    ) = supervisorScope {
        val scenarioLogger = scenario.logger ?: suiteLogger
        val scenarioTimeout = scenario.timeout ?: defaultTimeout
        val scenarioInputStrategy = scenario.inputStrategy ?: inputStrategy
        val otherInterceptors = scenario.interceptors + interceptors

        scenarioLogger.debug("Scenario '${scenario.name}'")
        scenarioLogger.debug("before runScenario")
        val testViewModel = TestViewModel(
            logger = scenarioLogger,
            testInterceptor = TestInterceptor(),
            otherInterceptors = otherInterceptors.map { it() },
            initialState = scenario.givenBlock?.invoke()
                ?: defaultInitialStateBlock?.invoke()
                ?: error("No initial state given"),
            inputHandler = inputHandler,
            filter = filter?.let { TestInputFilter(it) },
            inputStrategy = scenarioInputStrategy,
            name = scenario.name,
        )

        // start running the VM in the background
        val viewModelJob = launch(start = CoroutineStart.UNDISPATCHED) {
            testViewModel.impl.start(this) { testViewModel }
        }
        val eventHandlerJob = launch(start = CoroutineStart.UNDISPATCHED) {
            testViewModel.impl.attachEventHandler(TestEventHandler(eventHandler))
        }

        val inputSequenceScope = ViewModelTestScenarioInputSequenceScopeImpl(scenarioLogger, testViewModel)

        // run the scenario input sequence
        scenarioLogger.debug("    before onInputSequenceBlock")
        scenario.onInputSequenceBlock(inputSequenceScope)
        scenarioLogger.debug("    after onInputSequenceBlock")

        scenarioLogger.debug("    before awaitSideEffectsCompletion")
        withTimeoutOrNull(scenarioTimeout) {
            testViewModel.impl.awaitSideEffectsCompletion()
        }
        scenarioLogger.debug("    after awaitSideEffectsCompletion")

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
        scenario.verifyBlock(testViewModel.testInterceptor.getResults())
        scenarioLogger.debug("    after verification")

        scenarioLogger.debug("after runScenario")
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

        data class Skipped<Inputs : Any, Events : Any, State : Any>(
            override val scenario: ViewModelTestScenarioScopeImpl<Inputs, Events, State>,
        ) : ScenarioResult<Inputs, Events, State>() {
            override fun printResults(): String {
                return "Scenario '${scenario.name}': Skipped"
            }
        }
    }

    @ExperimentalCoroutinesApi
    internal suspend fun runTest() = supervisorScope {
        val totalTestTime = measureTime {
            val hasSoloScenario = scenarioBlocks.any { it.solo }

            val actualScenariosToRun = if (hasSoloScenario) {
                scenarioBlocks.filter { it.solo }
            } else {
                scenarioBlocks
            }

            val results: List<ScenarioResult<Inputs, Events, State>> = actualScenariosToRun
                .map { scenario ->
                    if (scenario.skip) {
                        CompletableDeferred(ScenarioResult.Skipped(scenario))
                    } else {
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
                    }
                }
                .awaitAll()

            results.forEach {
                suiteLogger.info(it.printResults())
            }

            results.filterIsInstance<ScenarioResult.Failed<*, *, *>>().firstOrNull()?.let {
                throw it.reason
            }
        }

        suiteLogger.info("All scenarios completed in $totalTestTime")
    }
}
