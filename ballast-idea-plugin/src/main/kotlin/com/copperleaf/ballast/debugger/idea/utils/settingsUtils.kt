package com.copperleaf.ballast.debugger.idea.utils

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public fun <T : Enum<T>> Settings.enum(
    key: String? = null,
    defaultValue: T,
    valueOf: (String) -> T,
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return this@enum.getStringOrNull(key ?: property.name)
            ?.let { storedValue -> runCatching { valueOf(storedValue) }.getOrNull() }
            ?: defaultValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this@enum[key ?: property.name] = value.name
    }
}
