package com.copperleaf.ballast.debugger.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Badge
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.di.LocalProject
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.now
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.EditorTextField
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState
import org.jetbrains.compose.splitpane.SplitterScope
import org.jetbrains.compose.splitpane.VerticalSplitPane
import java.awt.Cursor
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

val minSplitPaneSize = 48.dp

fun LocalDateTime.format(pattern: String): String {
    return this.toJavaLocalDateTime().format(
        DateTimeFormatter.ofPattern(pattern)
    )
}

fun LocalDateTime.ago(now: LocalDateTime = LocalDateTime.now()): Duration {
    return now - this
}

val LocalTimer = compositionLocalOf<LocalDateTime> { error("LocalDateTime not provided") }

@Composable
fun currentTimeAsState(): State<LocalDateTime> {
    return produceState(LocalDateTime.now()) {
        while (true) {
            delay(1000)
            value = LocalDateTime.now()
        }
    }
}

@Composable
fun StatusIcon(
    isActive: Boolean,
    activeText: String,
    inactiveText: String,
    icon: ImageVector,
    tint: Color? = if (isActive) {
        MaterialTheme.colors.secondary
    } else {
        null
    },
    count: Int = 0,
    modifier: Modifier = Modifier,
) {
    val text = if (isActive) {
        activeText
    } else {
        inactiveText
    }

    val animatedColor by animateColorAsState(tint ?: LocalContentColor.current.copy(alpha = LocalContentAlpha.current))

    TooltipArea(
        tooltip = {
            Card {
                Box(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                    Text(text)
                }
            }
        },
        modifier = modifier,
        content = {
            Box {
                Card(modifier = Modifier.align(Alignment.Center)) {
                    Box(Modifier.padding(4.dp)) {
                        Icon(icon, text, tint = animatedColor, modifier = Modifier.align(Alignment.Center))
                    }
                }

                AnimatedVisibility(count > 1, modifier = Modifier.align(Alignment.BottomEnd)) {
                    Badge { Text("$count") }
                }
            }
        }
    )
}

@Composable
fun SplitPane(
    splitPaneState: SplitPaneState,
    modifier: Modifier = Modifier,
    navigation: LazyListScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    HorizontalSplitPane(
        splitPaneState = splitPaneState,
        modifier = modifier,
    ) {
        first(minSize = minSplitPaneSize) {
            Box(Modifier.fillMaxSize()) {
                val scrollState = rememberLazyListState()

                // the list of all Connections
                LazyColumn(Modifier.fillMaxSize(), state = scrollState) {
                    navigation()
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState),
                )
            }
        }
        second(minSize = minSplitPaneSize) {
            Box(Modifier.fillMaxSize()) {
                content()
            }
        }
        splitter {
            ColoredSplitter(true)
        }
    }
}

@Composable
fun VSplitPane(
    splitPaneState: SplitPaneState,
    modifier: Modifier = Modifier,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
) {
    VerticalSplitPane(
        splitPaneState = splitPaneState,
        modifier = modifier,
    ) {
        first(minSize = minSplitPaneSize) {
            Box(Modifier.fillMaxSize()) {
                topContent()
            }
        }
        second(minSize = minSplitPaneSize) {
            Box(Modifier.fillMaxSize()) {
                bottomContent()
            }
        }
        splitter {
            ColoredSplitter(false)
        }
    }
}

fun Modifier.cursorForResize(
    isHorizontal: Boolean,
): Modifier = composed {
    val icon = if (isHorizontal) {
        PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR))
    } else {
        PointerIcon(Cursor(Cursor.S_RESIZE_CURSOR))
    }
    pointerHoverIcon(icon)
}

@Composable
fun Modifier.onHoverState(
    notHovering: @Composable () -> Modifier = { Modifier },
    hovering: @Composable () -> Modifier = { Modifier }
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    return this
        .hoverable(interactionSource)
        .then(if (isHovered) hovering() else notHovering())
}

fun SplitterScope.ColoredSplitter(isHorizontal: Boolean) {
    visiblePart {
        Box(
            Modifier
                .background(SolidColor(MaterialTheme.colors.onSurface), alpha = 0.12f)
                .then(if (isHorizontal) Modifier.width(1.dp).fillMaxHeight() else Modifier.height(1.dp).fillMaxWidth())
        )
    }
    handle {
        Box(
            Modifier
                .markAsHandle()
                .cursorForResize(isHorizontal)
                .onHoverState(
                    notHovering = { Modifier },
                    hovering = { Modifier.background(SolidColor(MaterialTheme.colors.secondary), alpha = 0.5f) },
                )
                .then(if (isHorizontal) Modifier.width(4.dp).fillMaxHeight() else Modifier.height(4.dp).fillMaxWidth())
        )
    }
}

@Composable
fun Modifier.highlight(enabled: Boolean = true): Modifier {
    val next = if (enabled) Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f)) else Modifier
    return this.then(next)
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun IntellijEditor(
    text: String,
    modifier: Modifier = Modifier,
    fileType: String = "txt",
) {
    val project = LocalProject.current

    val document = remember(text) {
        EditorFactory.getInstance().createDocument(StringUtil.convertLineSeparators(text))
    }

    SwingPanel(
        modifier = modifier,
        background = MaterialTheme.colors.surface,
        factory = { EditorTextField(document, project, FileTypes.PLAIN_TEXT, true) },
        update = { },
    )
}
