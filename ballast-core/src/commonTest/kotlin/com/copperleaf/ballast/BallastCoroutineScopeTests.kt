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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class BallastCoroutineScopeTests {
    @Test
    fun testDispatchers() = runTest {
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
                        assertEquals("Inputs", (it.actualInputCoroutineScopeInfo?.dispatcher as? NamedDispatcher?)?.name)
                        assertNotNull(it.actualInputCoroutineScopeInfo?.uncaughtExceptionHandler)
                        val inputContext = it.actualInputCoroutineScopeInfo?.fullContext
                        inputContext.also { }

                        assertEquals("Events", (it.actualEventCoroutineScopeInfo?.dispatcher as? NamedDispatcher?)?.name)
                        assertNotNull(it.actualEventCoroutineScopeInfo?.uncaughtExceptionHandler)
                        val eventContext = it.actualEventCoroutineScopeInfo?.fullContext
                        eventContext.also { }

                        assertEquals("SideJobs", (it.actualSideJobCoroutineScopeInfo?.dispatcher as? NamedDispatcher?)?.name)
                        assertNotNull(it.actualSideJobCoroutineScopeInfo?.uncaughtExceptionHandler)
                        val sideJobContext = it.actualSideJobCoroutineScopeInfo?.fullContext
                        sideJobContext.also { }

                        assertEquals("Interceptor", (it.actualInterceptorCoroutineScopeInfo?.dispatcher as? NamedDispatcher?)?.name)
                        assertNotNull(it.actualInterceptorCoroutineScopeInfo?.uncaughtExceptionHandler)
                        val interceptorContext = it.actualInterceptorCoroutineScopeInfo?.fullContext
                        interceptorContext.also { }

                        println()
                    }
                }
            }
        }
    }
}
