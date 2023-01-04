package com.copperleaf.ballast.debugger.idea.utils

import com.intellij.ide.util.PropertiesComponent
import com.russhwolf.settings.Settings

class PropertiesComponentSettings(
    private val prefix: String,
    private val properties: PropertiesComponent,
) : Settings {

    init {
        check(prefix.isNotBlank()) {
            "Prefix cannot be blank"
        }
    }

    override val keys: Set<String>
        get() = TODO("Not yet implemented")
    override val size: Int
        get() = TODO("Not yet implemented")

    override fun clear() {
        TODO("Not yet implemented")
    }

    private fun getKey(key: String): String {
        return "$prefix.$key"
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return properties.getValue(getKey(key))?.toBooleanStrictOrNull() ?: defaultValue
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return properties.getValue(getKey(key))?.toBooleanStrictOrNull()
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return properties.getValue(getKey(key))?.toDoubleOrNull() ?: defaultValue
    }

    override fun getDoubleOrNull(key: String): Double? {
        return properties.getValue(getKey(key))?.toDoubleOrNull()
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return properties.getValue(getKey(key))?.toFloatOrNull() ?: defaultValue
    }

    override fun getFloatOrNull(key: String): Float? {
        return properties.getValue(getKey(key))?.toFloatOrNull()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return properties.getValue(getKey(key))?.toIntOrNull() ?: defaultValue
    }

    override fun getIntOrNull(key: String): Int? {
        return properties.getValue(getKey(key))?.toIntOrNull()
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return properties.getValue(getKey(key))?.toLongOrNull() ?: defaultValue
    }

    override fun getLongOrNull(key: String): Long? {
        return properties.getValue(getKey(key))?.toLongOrNull()
    }

    override fun getString(key: String, defaultValue: String): String {
        return properties.getValue(getKey(key)) ?: defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return properties.getValue(getKey(key))
    }

    override fun hasKey(key: String): Boolean {
        return properties.getValue(getKey(key)) != null
    }

    override fun putBoolean(key: String, value: Boolean) {
        properties.setValue(getKey(key), value.toString())
    }

    override fun putDouble(key: String, value: Double) {
        properties.setValue(getKey(key), value.toString())
    }

    override fun putFloat(key: String, value: Float) {
        properties.setValue(getKey(key), value.toString())
    }

    override fun putInt(key: String, value: Int) {
        properties.setValue(getKey(key), value.toString())
    }

    override fun putLong(key: String, value: Long) {
        properties.setValue(getKey(key), value.toString())
    }

    override fun putString(key: String, value: String) {
        properties.setValue(getKey(key), value)
    }

    override fun remove(key: String) {
        properties.unsetValue(getKey(key))
    }
}
