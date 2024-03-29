@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState

@Composable
internal fun DebuggerScaffold(
    primaryToolbar: (@Composable RowScope.() -> Unit)? = null,
    tabs: (@Composable RowScope.() -> Unit)? = null,
    mainContentLeft: (@Composable ColumnScope.() -> Unit)? = null,
    contentLeftToolbar: (@Composable ColumnScope.() -> Unit)? = null,
    mainContentRight: (@Composable ColumnScope.() -> Unit)? = null,
    contentRightToolbar: (@Composable ColumnScope.() -> Unit)? = null,
    secondaryContent: (@Composable ColumnScope.() -> Unit)? = null,

    stickyContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(Modifier.fillMaxSize()) {
        if (primaryToolbar != null) {
            Row {
                primaryToolbar()
            }
        }

        Row(Modifier.fillMaxWidth().weight(1f)) {
            Column(Modifier.fillMaxHeight().weight(1f)) {
                if (tabs != null) {
                    Row {
                        tabs()
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    val mainContentLeftLambda = @Composable {
                        Row(Modifier.fillMaxSize()) {
                            if (contentLeftToolbar != null) {
                                Column {
                                    contentLeftToolbar()
                                }
                            }
                            Column(Modifier.fillMaxHeight().weight(1f)) {
                                mainContentLeft?.invoke(this)
                            }
                        }
                    }
                    val mainContentRightLambda = @Composable {
                        Row(Modifier.fillMaxSize()) {
                            Column(Modifier.fillMaxHeight().weight(1f)) {
                                mainContentRight?.invoke(this)
                            }
                            if (contentRightToolbar != null) {
                                Column {
                                    contentRightToolbar()
                                }
                            }
                        }
                    }
                    val stickyContentLambda = @Composable {
                        Row(Modifier.fillMaxSize()) {
                            Column(Modifier.fillMaxHeight().weight(1f)) {
                                stickyContent?.invoke(this)
                            }
                        }
                    }

                    if (mainContentLeft != null && mainContentRight != null && stickyContent != null) {
                        HorizontalSplitPane(
                            splitPaneState = rememberSplitPaneState(0.35f),
                        ) {
                            first(minSize = 60.dp) { mainContentLeftLambda() }
                            second() {
                                Row(Modifier.fillMaxSize()) {
                                    HorizontalSplitPane(
                                        splitPaneState = rememberSplitPaneState(0.35f),
                                    ) {
                                        first(minSize = 60.dp) { mainContentRightLambda() }
                                        second() { stickyContentLambda() }
                                    }
                                }
                            }
                        }
                    }
                    else if (mainContentLeft != null && mainContentRight != null) {
                        HorizontalSplitPane(
                            splitPaneState = rememberSplitPaneState(0.35f),
                        ) {
                            first(minSize = 60.dp) {
                                mainContentLeftLambda()
                            }
                            second() {
                                mainContentRightLambda()
                            }
                        }
                    }
                    else if (mainContentLeft != null && stickyContent != null) {
                        HorizontalSplitPane(
                            splitPaneState = rememberSplitPaneState(0.35f),
                        ) {
                            first(minSize = 60.dp) {
                                mainContentLeftLambda()
                            }
                            second() {
                                stickyContentLambda()
                            }
                        }
                    }
                    else if(mainContentLeft != null) {
                        mainContentLeftLambda()
                    }
                    else if(mainContentRight != null) {
                        error("use mainContentLeft for a single-panel view instead")
                    }
                }
                if (secondaryContent != null) {
                    Column(Modifier.fillMaxWidth().wrapContentHeight()) {
                        secondaryContent()
                    }
                }
            }
        }
    }
}
