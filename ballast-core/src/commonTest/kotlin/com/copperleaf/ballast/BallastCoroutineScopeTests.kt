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
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class BallastCoroutineScopeTests : StringSpec({
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
                        (it.actualInputCoroutineScopeInfo?.dispatcher as? NamedDispatcher?)?.name shouldBe "Inputs"
                        (it.actualInputCoroutineScopeInfo?.uncaughtExceptionHandler) shouldNotBe null
                        val inputContext = it.actualInputCoroutineScopeInfo?.fullContext
                        inputContext.also { }

                        (it.actualEventCoroutineScopeInfo?.dispatcher as? NamedDispatcher?)?.name shouldBe "Events"
                        (it.actualEventCoroutineScopeInfo?.uncaughtExceptionHandler) shouldNotBe null
                        val eventContext = it.actualEventCoroutineScopeInfo?.fullContext
                        eventContext.also { }

                        (it.actualSideJobCoroutineScopeInfo?.dispatcher as? NamedDispatcher?)?.name shouldBe "SideJobs"
                        (it.actualSideJobCoroutineScopeInfo?.uncaughtExceptionHandler) shouldNotBe null
                        val sideJobContext = it.actualSideJobCoroutineScopeInfo?.fullContext
                        sideJobContext.also { }

                        (it.actualInterceptorCoroutineScopeInfo?.dispatcher as? NamedDispatcher?)?.name shouldBe "Interceptor"
                        (it.actualInterceptorCoroutineScopeInfo?.uncaughtExceptionHandler) shouldNotBe null
                        val interceptorContext = it.actualInterceptorCoroutineScopeInfo?.fullContext
                        interceptorContext.also { }

                        println()
                    }
                }
            }
        }
    }
})
