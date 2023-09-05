package com.copperleaf.ballast.debugger.idea.features.debugger

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.debugger.idea.features.debugger.vm.DebuggerUiContract
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.StringSelection

@Suppress("UNUSED_EXPRESSION")
class DebuggerUiEventHandler() : EventHandler<
        DebuggerUiContract.Inputs,
        DebuggerUiContract.Events,
        DebuggerUiContract.State> {
    override suspend fun EventHandlerScope<
            DebuggerUiContract.Inputs,
            DebuggerUiContract.Events,
            DebuggerUiContract.State>.handleEvent(
        event: DebuggerUiContract.Events
    ) = when (event) {
        is DebuggerUiContract.Events.CopyToClipboard -> {
            CopyPasteManager.getInstance().setContents(StringSelection(event.text))
        }
    }
}
