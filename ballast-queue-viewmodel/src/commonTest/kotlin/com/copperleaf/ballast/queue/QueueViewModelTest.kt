@file:OptIn(ExperimentalCoroutinesApi::class)

package com.copperleaf.ballast.queue

import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.queue.driver.SyncQueueDriver
import com.copperleaf.ballast.queue.vm.TestContract
import com.copperleaf.ballast.queue.vm.TestInputHandler
import com.copperleaf.ballast.queue.vm.TestSyncQueueAdapter
import com.copperleaf.ballast.test.viewModelTest
import com.copperleaf.ballast.withSerialization
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class QueueViewModelTest {

    @Test
    fun test() = runTest {
        viewModelTest(
            inputHandler = TestInputHandler(),
            eventHandler = eventHandler { },
        ) {
            defaultInitialState { TestContract.State() }

            scenario("test a queue-backed Viewmodel") {
                val driver = SyncQueueDriver()
                inputStrategy {
                    JobQueueInputStrategy(
                        queueName = "test-queue",
                        driver = driver,
                        adapter = TestSyncQueueAdapter(),
                        captureErrorStacktrace = false,
                    )
                }
                customizeConfiguration {
                    it.withSerialization(
                        inputsSerializer = TestContract.Inputs.serializer(),
                        eventsSerializer = TestContract.Events.serializer(),
                        stateSerializer = TestContract.State.serializer(),
                        json = Json.Default,
                    )
                }

                running {
                    +TestContract.Inputs.AsyncJob("one")
                }
                resultsIn {
                    assertEquals(
                        actual = states,
                        expected = listOf(
                            TestContract.State(),
                            TestContract.State(step = 1),
                            TestContract.State(step = 2),
                            TestContract.State(step = 3),
                        ),
                    )
                    assertEquals(
                        actual = events,
                        expected = listOf(
                            TestContract.Events.JobCompleted("ONE"),
                        ),
                    )

                    assertEquals(
                        actual = driver.lastJob?.serializedPayload,
                        expected = buildJsonObject {
                            put("type", "com.copperleaf.ballast.queue.vm.TestContract.Inputs.AsyncJob")
                            put("inputData", "one")
                        }.toString(),
                    )
                    assertEquals(
                        actual = driver.lastJob?.serializedState,
                        expected = buildJsonObject {
                        }.toString(),
                    )
                    assertEquals(
                        actual = driver.lastJobResultType,
                        expected = JobCompletionResultType.Success,
                    )
                    assertEquals(
                        actual = driver.lastJobResultData,
                        expected = buildJsonObject {
                            put("type", "com.copperleaf.ballast.queue.vm.TestContract.Events.JobCompleted")
                            put("result", "ONE")
                        }.toString(),
                    )
                    assertEquals(
                        actual = driver.lastJobFailureMessage,
                        expected = null,
                    )
                }
            }
        }
    }
}
