package com.copperleaf.ballast.examples.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Composable
fun JsonTreeView(jsonString: String?, json: Json) {
    if (jsonString == null) {
        return
    }

    val reformattedJson = remember(jsonString) {
        val parsedJson = json.parseToJsonElement(jsonString)
        json.encodeToString(JsonElement.serializer(), parsedJson)
    }

    Card {
        Box(Modifier.padding(8.dp)) {
            Text(reformattedJson, fontFamily = FontFamily.Monospace)
        }
    }
}
