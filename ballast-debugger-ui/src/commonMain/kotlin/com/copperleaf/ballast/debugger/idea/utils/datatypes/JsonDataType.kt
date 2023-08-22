package com.copperleaf.ballast.debugger.idea.utils.datatypes

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

public class JsonDataType : DataType {
    private val prettyJson = Json { this.prettyPrint = true }

    override fun reformat(input: String): String {
        val parsed = Json.decodeFromString(JsonElement.serializer(), input)
        return prettyJson.encodeToString(JsonElement.serializer(), parsed)
    }

    override fun parseToJson(input: String): JsonElement {
        TODO("Not yet implemented")
    }
}
