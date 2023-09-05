package com.copperleaf.ballast.debugger.idea.utils.datatypes

import io.ktor.http.ContentType
import kotlinx.serialization.json.JsonElement

public interface DataType {
    public fun reformat(input: String): String

    public fun parseToJson(input: String): JsonElement

    public companion object {
        public fun getForMimeType(mimeType: ContentType): DataType {
            return when (mimeType) {
                ContentType.Application.Json -> JsonDataType()
                else -> ToStringDataType()
            }
        }
    }
}
