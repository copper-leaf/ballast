package com.copperleaf.ballast.examples.mainlist

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

object MainWebUi {
    @Composable
    public fun WebContent(
        uiState: MainContract.State,
        postInput: (MainContract.Inputs) -> Unit,
    ) {
        BulmaPanel(
            headingStart = { Text("Navigation") },
        ) {
            Div(attrs = {
                onClick { postInput(MainContract.Inputs.GoToCounter) }
            }) { Text("Counter") }
            Div(attrs = {
                onClick { postInput(MainContract.Inputs.GoToBoardGameGeek) }
            }) { Text("BGG") }
            Div(attrs = {
                onClick { postInput(MainContract.Inputs.GoToScorekeeper) }
            }) { Text("Scorekeeper") }
            Div(attrs = {
                onClick { postInput(MainContract.Inputs.GoToKitchenSink) }
            }) { Text("Kitchen Sink") }
        }
    }
}
