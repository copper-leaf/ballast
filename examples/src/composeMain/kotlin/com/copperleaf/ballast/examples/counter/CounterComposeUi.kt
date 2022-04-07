package com.copperleaf.ballast.examples.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

object CounterComposeUi {

    @Composable
    public fun Content(
        uiState: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingActionButton(
                onClick = { postInput(CounterContract.Inputs.Decrement(1)) }
            ) {
                Icon(Icons.Default.Add, "Decrement")
            }

            Text(
                text = "${uiState.count}",
                style = MaterialTheme.typography.h3,
            )

            FloatingActionButton(
                onClick = { postInput(CounterContract.Inputs.Increment(1)) }
            ) {
                Icon(Icons.Default.Add, "Increment")
            }
        }
    }

}
