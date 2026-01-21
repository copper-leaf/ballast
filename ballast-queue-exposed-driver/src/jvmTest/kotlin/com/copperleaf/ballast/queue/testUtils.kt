package com.copperleaf.ballast.queue

import com.copperleaf.ballast.queue.driver.DatabaseQueueDriver
import com.copperleaf.ballast.queue.driver.JobsTable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.v1.core.ResultRow
import kotlin.test.assertEquals

fun JobsTable.assertJobEquals(
    rows: List<ResultRow>,
    expected: List<SerializedJob<DatabaseQueueDriver.Metadata>>,
) {
    rows.zip(expected).forEach { (row, expectedJob) ->
        assertJobEquals(row, expectedJob)
    }
}

fun JobsTable.assertJobEquals(
    row: ResultRow,
    expected: SerializedJob<DatabaseQueueDriver.Metadata>,
) {
    assertEquals(message = "queue", actual = row[queue], expected = expected.queueName)
    assertEquals(message = "payload", actual = row[payload].testJson(), expected = expected.serializedPayload.testJson())
    assertEquals(message = "timeout", actual = row[timeout_duration], expected = expected.timeoutDuration)
    assertEquals(message = "state", actual = row[job_state].testJson(), expected = expected.serializedState.testJson())
    assertEquals(message = "result_data", actual = row[result_data].testJson(), expected = expected.serializedResultData.testJson())

    assertEquals(message = "max_attempts", actual = row[max_attempts], expected = expected.metadata.maxAttempts)
    assertEquals(message = "priority", actual = row[priority], expected = expected.metadata.priority)
    assertEquals(message = "run_at", actual = row[run_at], expected = expected.metadata.runAt)
    assertEquals(message = "status", actual = row[status], expected = expected.metadata.status)
    assertEquals(message = "attempts", actual = row[attempts], expected = expected.metadata.attempts)
    assertEquals(message = "last_run_finished_at", actual = row[last_run_finished_at], expected = expected.metadata.lastRunFinishedAt)
    assertEquals(message = "last_run_duration", actual = row[last_run_duration], expected = expected.metadata.lastRunDuration)
    assertEquals(message = "last_run_result_type", actual = row[last_run_result_type], expected = expected.metadata.lastResultType)
    assertEquals(message = "last_run_failure_message", actual = row[last_run_failure_message], expected = expected.metadata.lastErrorMessage)
    assertEquals(message = "last_run_failure_stacktrace", actual = row[last_run_failure_stacktrace], expected = expected.metadata.lastStacktrace)
}

private fun JsonElement?.testJson(json: Json = Json { prettyPrint = false }): JsonElement? {
    return this
}

private fun String?.testJson(json: Json = Json { prettyPrint = false }): JsonElement? {
    return json.decodeFromString(JsonElement.serializer(), this ?: return null)
}
