package com.copperleaf.ballast.debugger.idea.utils

fun <T> List<T>?.maybeFilter(searchQuery: String, getFields: (T) -> List<String>): List<T> {
    if (this == null) return emptyList()
    if (searchQuery.isBlank()) return this

    return this.filter {
        getFields(it).any { it.contains(searchQuery) }
    }
}
