package com.copperleaf.ballast.examples.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.presentation.models.QueueName
import com.copperleaf.ballast.examples.presentation.ui.MainScreenContract

@Composable
fun ColumnScope.NewJobHeader(
    postInput: (MainScreenContract.Inputs) -> Unit,
) {
    var queueName by remember { mutableStateOf(QueueName.Default) }
    var timeoutSeconds by remember { mutableStateOf(30) }
    var retryDelaySeconds by remember { mutableStateOf(10) }
    var maxAttempts by remember { mutableStateOf(5) }
    var successAttemptIndex by remember { mutableStateOf(2) }
    var processingTimeSeconds by remember { mutableStateOf(10) }

    var deduplicationKey: String by remember { mutableStateOf("") }
    var deduplicationDuration: Int by remember { mutableStateOf(0) }
    var resultValue by remember { mutableStateOf("Result") }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        DropdownSelector(
            label = { Text("Queue Name") },
            value = queueName,
            onValueChange = { queueName = it },
            allEnumValues = QueueName.entries,
            modifier = Modifier.weight(1f),
        )

        OutlinedTextField(
            value = timeoutSeconds.toString(),
            onValueChange = { timeoutSeconds = it.toIntOrNull() ?: timeoutSeconds },
            label = { Text("Timeout (s)") },
            modifier = Modifier.weight(1f),
        )

        OutlinedTextField(
            value = retryDelaySeconds.toString(),
            onValueChange = { retryDelaySeconds = it.toIntOrNull() ?: retryDelaySeconds },
            label = { Text("Retry Delay (s)") },
            modifier = Modifier.weight(1f),
        )

        OutlinedTextField(
            value = maxAttempts.toString(),
            onValueChange = { maxAttempts = it.toIntOrNull() ?: maxAttempts },
            label = { Text("Max Attempts") },
            modifier = Modifier.weight(1f),
        )

        OutlinedTextField(
            value = successAttemptIndex.toString(),
            onValueChange = { successAttemptIndex = it.toIntOrNull() ?: successAttemptIndex },
            label = { Text("Success Attempt Index") },
            modifier = Modifier.weight(1f),
        )

        OutlinedTextField(
            value = processingTimeSeconds.toString(),
            onValueChange = {
                processingTimeSeconds = it.toIntOrNull() ?: processingTimeSeconds
            },
            label = { Text("Processing Time (s)") },
            modifier = Modifier.weight(1f),
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        OutlinedTextField(
            value = deduplicationKey,
            onValueChange = { deduplicationKey = it },
            label = { Text("Deduplication key") },
            modifier = Modifier.weight(1f),
        )

        OutlinedTextField(
            value = deduplicationDuration.toString(),
            onValueChange = { deduplicationDuration = it.toIntOrNull() ?: deduplicationDuration },
            label = { Text("Deduplication duration (s)") },
            modifier = Modifier.weight(1f),
        )

        OutlinedTextField(
            value = resultValue,
            onValueChange = { resultValue = it },
            label = { Text("Result Value") },
            modifier = Modifier.weight(1f),
        )

        Button(
            modifier = Modifier.weight(1f),
            onClick = {
                postInput(
                    MainScreenContract.Inputs.EnqueueNewJob(
                        queueName = queueName,
                        timeoutSeconds = timeoutSeconds,
                        retryDelaySeconds = retryDelaySeconds,
                        maxAttempts = maxAttempts,
                        successAttemptIndex = successAttemptIndex,
                        processingTimeSeconds = processingTimeSeconds,
                        deduplicationKey = deduplicationKey,
                        deduplicationDuration = deduplicationDuration,
                        resultValue = resultValue,
                    )
                )
            }
        ) {
            Text("Enqueue")
        }
    }
}
