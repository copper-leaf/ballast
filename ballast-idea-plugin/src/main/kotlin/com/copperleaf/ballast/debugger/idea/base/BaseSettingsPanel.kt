package com.copperleaf.ballast.debugger.idea.base

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/**
 * Allow the user to configure the port of the debugger server
 *
 * See https://plugins.jetbrains.com/docs/intellij/settings-guide.html#the-configurable-interface
 */
@Suppress("UnstableApiUsage")
abstract class BaseSettingsPanel : Configurable {

    protected abstract val project: Project

    private var panel: DialogPanel? = null

    override fun createComponent(): JComponent? {
        return panel {
            Content()
        }.also {
            panel = it
        }
    }

    abstract fun Panel.Content()

    override fun reset() {
        super.reset()
        panel?.reset()
        println("reset settings")
    }

    override fun isModified(): Boolean {
        println("checking if settings modified")
        return panel?.isModified() ?: false
    }

    override fun apply() {
        println("applying settings")
        panel?.apply()
    }
}
