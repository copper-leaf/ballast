package com.copperleaf.ballast.debugger.idea.settings

import com.copperleaf.ballast.debugger.di.BallastDebuggerInjector
import com.copperleaf.ballast.debugger.idea.base.BaseSettingsPanel
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindIntText

/**
 * Allow the user to configure the port of the debugger server
 *
 * See https://plugins.jetbrains.com/docs/intellij/settings-guide.html#the-configurable-interface
 */
@Suppress("UnstableApiUsage")
class BallastPluginSettingsPanel(
    override val project: Project
) : BaseSettingsPanel() {

    private val injector by lazy {
        BallastDebuggerInjector.getInstance(project)
    }

    private var initialDebuggerPort: Int = injector.prefs.debuggerPort
    var debuggerPort: Int = initialDebuggerPort
        set(value) {
            println("setting debuggerPort to $value")
            field = value
        }

    override fun Panel.Content() {
        onReset { debuggerPort = initialDebuggerPort }
        onIsModified { debuggerPort != initialDebuggerPort }
        onApply {
            injector.prefs.debuggerPort = debuggerPort
            initialDebuggerPort = injector.prefs.debuggerPort
        }
        row("Debugger port (default: 9684)") {
            intTextField(1024..65535).bindIntText(::debuggerPort)
        }
    }

    override fun getDisplayName(): String {
        return "Ballast"
    }
}
