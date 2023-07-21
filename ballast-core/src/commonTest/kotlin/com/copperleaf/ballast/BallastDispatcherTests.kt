package com.copperleaf.ballast

import com.copperleaf.ballast.contracts.dispatcher.DispatcherTestContract
import com.copperleaf.ballast.contracts.dispatcher.DispatcherTestEventHandler
import com.copperleaf.ballast.contracts.dispatcher.DispatcherTestInputHandler
import com.copperleaf.ballast.contracts.dispatcher.DispatcherTestInterceptor
import com.copperleaf.ballast.contracts.dispatcher.NamedDispatcher
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.core.PrintlnLogger
import com.copperleaf.ballast.test.viewModelTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class BallastDispatcherTests : StringSpec({
    "test dispatchers" {
        viewModelTest(
            inputHandler = DispatcherTestInputHandler(),
            eventHandler = DispatcherTestEventHandler(),
        ) {
            defaultInputStrategy { FifoInputStrategy.typed() }
            defaultInitialState { DispatcherTestContract.State() }
            logger { PrintlnLogger(it) }
            addInterceptor { LoggingInterceptor() }
            addInterceptor { DispatcherTestInterceptor() }

            scenario("test dispatchers") {
                customizeConfiguration {
                    it.dispatchers(
                        inputsDispatcher = NamedDispatcher("Inputs", it.inputsDispatcher),
                        eventsDispatcher = NamedDispatcher("Events", it.eventsDispatcher),
                        sideJobsDispatcher = NamedDispatcher("SideJobs", it.sideJobsDispatcher),
                        interceptorDispatcher = NamedDispatcher("Interceptor", it.interceptorDispatcher),
                    )
                }

                running {
                    +DispatcherTestContract.Inputs.Initialize
                }
                resultsIn {
                    latestState.let {
                        (it.actualInputDispatcher as? NamedDispatcher?)?.name shouldBe "Inputs"
                        (it.actualEventDispatcher as? NamedDispatcher?)?.name shouldBe "Events"
                        (it.actualSideJobDispatcher as? NamedDispatcher?)?.name shouldBe "SideJobs"
                        (it.actualInterceptorDispatcher as? NamedDispatcher?)?.name shouldBe "Interceptor"
                    }
                }
            }
        }
    }
})
