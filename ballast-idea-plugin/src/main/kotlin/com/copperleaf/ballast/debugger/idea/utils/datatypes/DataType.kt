package com.copperleaf.ballast.debugger.idea.utils.datatypes

import io.ktor.http.ContentType
import kotlinx.serialization.json.JsonElement

interface DataType {
    fun reformat(input: String): String

    fun parseToJson(input: String): JsonElement

    companion object {
        fun getForMimeType(mimeType: ContentType): DataType {
            return when(mimeType) {
                ContentType.Application.Json -> JsonDataType()
                else -> ToStringDataType()
            }
        }
    }
}
