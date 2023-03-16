package com.copperleaf.ballast.debugger.idea.utils.datatypes

import kotlinx.serialization.json.JsonElement

class ToStringDataType : DataType {
    override fun reformat(input: String): String {
        return input
    }

    override fun parseToJson(input: String): JsonElement {
        TODO("Not yet implemented")
    }
}
