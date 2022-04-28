package com.copperleaf.ballast.debugger.idea.base

import com.copperleaf.ballast.debugger.idea.settings.BallastPluginPrefs
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BasePluginPrefs(
    private val project: Project,
    private val prefix: String,
) : BallastPluginPrefs {
    init {
        check(prefix.isNotBlank()) { "Prefix cannot be blank!" }
    }

    private val properties get() = PropertiesComponent.getInstance(project)

    protected fun string(defaultValue: String = ""): ReadWriteProperty<BasePluginPrefs, String> = StringValue(defaultValue)
    protected fun int(defaultValue: Int = 0): ReadWriteProperty<BasePluginPrefs, Int> = IntValue(defaultValue)
    protected fun float(defaultValue: Float = 0f): ReadWriteProperty<BasePluginPrefs, Float> = FloatValue(defaultValue)
    protected fun <T: Enum<T>> enum(defaultValue: T, valueOf: (String)->T, ) : ReadWriteProperty<BasePluginPrefs, T> = EnumValue(defaultValue, valueOf)

    private inner class StringValue(private val defaultValue: String) : ReadWriteProperty<BasePluginPrefs, String> {
        override fun getValue(thisRef: BasePluginPrefs, property: KProperty<*>): String {
            return properties.getValue("${prefix}.${property.name}", defaultValue)
        }

        override fun setValue(thisRef: BasePluginPrefs, property: KProperty<*>, value: String) {
            properties.setValue("${prefix}.${property.name}", value, defaultValue)
        }
    }

    private inner class IntValue(private val defaultValue: Int) : ReadWriteProperty<BasePluginPrefs, Int> {
        override fun getValue(thisRef: BasePluginPrefs, property: KProperty<*>): Int {
            return properties.getInt("${prefix}.${property.name}", defaultValue)
        }

        override fun setValue(thisRef: BasePluginPrefs, property: KProperty<*>, value: Int) {
            properties.setValue("${prefix}.${property.name}", value, defaultValue)
        }
    }

    private inner class FloatValue(private val defaultValue: Float) : ReadWriteProperty<BasePluginPrefs, Float> {
        override fun getValue(thisRef: BasePluginPrefs, property: KProperty<*>): Float {
            return properties.getFloat("${prefix}.${property.name}", defaultValue)
        }

        override fun setValue(thisRef: BasePluginPrefs, property: KProperty<*>, value: Float) {
            properties.setValue("${prefix}.${property.name}", value, defaultValue)
        }
    }

    private inner class EnumValue<T: Enum<T>>(
        private val defaultValue: T,
        private val valueOf: (String)->T,
    ) : ReadWriteProperty<BasePluginPrefs, T> {
        override fun getValue(thisRef: BasePluginPrefs, property: KProperty<*>): T {
            return properties.getValue("${prefix}.${property.name}")
                ?.let { storedValue -> runCatching { valueOf(storedValue) }.getOrNull() }
                ?: defaultValue
        }

        override fun setValue(thisRef: BasePluginPrefs, property: KProperty<*>, value: T) {
            properties.setValue("${prefix}.${property.name}", value.name, defaultValue.name)
        }
    }
}
