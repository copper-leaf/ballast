package com.copperleaf.ballast.examples.mainlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.copperleaf.ballast.examples.mainlist.MainContract

object MainComposeUi {

    @Composable
    public fun Content(
        uiState: MainContract.State,
        postInput: (MainContract.Inputs) -> Unit,
    ) {
        Column {
            ListItem(Modifier.clickable { postInput(MainContract.Inputs.GoToCounter) }) { Text("Counter") }
            ListItem(Modifier.clickable { postInput(MainContract.Inputs.GoToBoardGameGeek) }) { Text("BGG") }
            ListItem(Modifier.clickable { postInput(MainContract.Inputs.GoToScorekeeper) }) { Text("Scorekeeper") }
            ListItem(Modifier.clickable { postInput(MainContract.Inputs.GoToKitchenSink) }) { Text("Kitchen Sink") }
        }
    }

}
