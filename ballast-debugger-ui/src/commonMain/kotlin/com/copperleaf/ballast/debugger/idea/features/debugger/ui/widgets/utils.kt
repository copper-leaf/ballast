@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.features.debugger.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Badge
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonColors
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.copperleaf.ballast.debugger.idea.features.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.settings.DebuggerUiSettings
import com.copperleaf.ballast.debugger.idea.utils.datatypes.DataType
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.now
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrNull
import io.ktor.http.ContentType
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

public val minSplitPaneSize: Dp = 48.dp

public fun LocalDateTime.format(pattern: String = "hh:mm:ss a"): String {
    return this.toJavaLocalDateTime().format(
        DateTimeFormatter.ofPattern(pattern)
    )
}

public fun LocalDateTime.ago(now: LocalDateTime = LocalDateTime.now()): Duration {
    return now - this
}

public val LocalTimer: ProvidableCompositionLocal<LocalDateTime> = compositionLocalOf<LocalDateTime> { error("LocalDateTime not provided") }

@Composable
public fun currentTimeAsState(): State<LocalDateTime> {
    return produceState(LocalDateTime.now()) {
        while (true) {
            delay(1000)
            value = LocalDateTime.now()
        }
    }
}

@Composable
public fun ProvideTime(content: @Composable () -> Unit) {
    val time by currentTimeAsState()

    CompositionLocalProvider(LocalTimer provides time) {
        content()
    }
}

@Composable
public fun StatusIcon(
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
public fun SplitPane(
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
public fun VSplitPane(
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

public fun Modifier.cursorForResize(
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
public fun Modifier.onHoverState(
    notHovering: @Composable () -> Modifier = { Modifier },
    hovering: @Composable () -> Modifier = { Modifier }
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    return this
        .hoverable(interactionSource)
        .then(if (isHovered) hovering() else notHovering())
}

public fun SplitterScope.ColoredSplitter(isHorizontal: Boolean) {
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
public fun Modifier.highlight(enabled: Boolean = true): Modifier {
    val next = if (enabled) Modifier.background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f)) else Modifier
    return this.then(next)
}

@Suppress("UNUSED_PARAMETER")
@Composable
public fun IntellijEditor(
    text: String,
    contentType: ContentType,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface,
    onContentCopied: ((String) -> Unit)? = null,
) {
    Box(modifier.fillMaxSize().background(Color(51, 51, 51))) {
        if(onContentCopied != null) {
            ToolBarActionIconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(10f),
                imageVector = Icons.Default.CopyAll,
                contentDescription = "Copy",
                enabled = true,
                onClick = {
                    onContentCopied(text)
                }
            )
        }

        Column(Modifier.zIndex(5f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            val lines = remember(text, contentType) {
                val dataTypeConverter = DataType.getForMimeType(contentType)
                val reformattedText = dataTypeConverter.reformat(text)
                reformattedText.lines()
            }

            SelectionContainer(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth()) {
                    lines.forEachIndexed { index, line ->
                        Row(Modifier.fillMaxWidth()) {
                            DisableSelection {
                                Text("${index + 1}", Modifier.width(24.dp))
                            }
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = line,
                                color = color,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
public fun rememberConnectionCurrentDestination(
    connection: BallastConnectionState?,
    settings: Cached<DebuggerUiSettings>,
): State<String?> {
    return viewModelValue {
        val uiSettings = settings.getCachedOrNull()
        if (uiSettings == null) {
            null
        } else if (!uiSettings.showCurrentRoute) {
            null
        } else {
            connection
                ?.viewModels
                ?.find { it.viewModelName == uiSettings.routerViewModelName }
                ?.states
                ?.firstOrNull()
                ?.serializedValue
                ?.let {
                    it
                        .trim()
                        .removePrefix("RouterContract(backstack=[")
                        .removeSuffix("])")
                        .trim()
                        .lines()
                        .map { line -> line.trim().trim('\'').removeSuffix(",") }
                        .lastOrNull()
                }
        }
    }
}

public fun getRouteForSelectedViewModel(
    currentRoute: DebuggerRoute,
    connectionId: String,
    viewModelName: String?,
): String {
    return if (viewModelName != null) {
        val newRoute = when (currentRoute) {
            DebuggerRoute.Home -> DebuggerRoute.Home
            DebuggerRoute.Connection -> DebuggerRoute.ViewModelStates
            DebuggerRoute.ViewModelStates -> DebuggerRoute.ViewModelStates
            DebuggerRoute.ViewModelStateDetails -> DebuggerRoute.ViewModelStates
            DebuggerRoute.ViewModelInputs -> DebuggerRoute.ViewModelInputs
            DebuggerRoute.ViewModelInputDetails -> DebuggerRoute.ViewModelInputs
            DebuggerRoute.ViewModelEvents -> DebuggerRoute.ViewModelEvents
            DebuggerRoute.ViewModelEventDetails -> DebuggerRoute.ViewModelEvents
            DebuggerRoute.ViewModelSideJobs -> DebuggerRoute.ViewModelSideJobs
            DebuggerRoute.ViewModelSideJobDetails -> DebuggerRoute.ViewModelSideJobs
            DebuggerRoute.ViewModelInterceptors -> DebuggerRoute.ViewModelInterceptors
            DebuggerRoute.ViewModelInterceptorDetails -> DebuggerRoute.ViewModelInterceptors
            DebuggerRoute.ViewModelLogs -> DebuggerRoute.ViewModelLogs
//            DebuggerRoute.ViewModelTimeline -> DebuggerRoute.ViewModelTimeline
        }

        newRoute
            .directions()
            .pathParameter("connectionId", connectionId)
            .pathParameter("viewModelName", viewModelName)
            .build()

    } else {
        DebuggerRoute.Connection
            .directions()
            .pathParameter("connectionId", connectionId)
            .build()
    }
}

@Composable
public fun Section(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Box(Modifier.padding(top = 4.dp, bottom = 8.dp, start = 0.dp, end = 0.dp)) {
            ProvideTextStyle(MaterialTheme.typography.subtitle1) {
                title()
            }
        }
        Divider()
        Column(
            Modifier.padding(top = 8.dp, bottom = 8.dp, start = 0.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content()
        }
    }
}

@Composable
public fun CheckboxArea(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
            )
            .padding(top = 2.dp, bottom = 2.dp, start = 2.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
        )
        Row(Modifier.padding(start = 8.dp)) {
            content()
        }
    }
}

@Composable
public fun RadioButtonArea(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    text: @Composable () -> Unit,
) {
    Row(
        modifier
            .toggleable(
                value = selected,
                onValueChange = { onClick?.invoke() },
            )
            .padding(top = 2.dp, bottom = 2.dp, start = 2.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Row(Modifier.padding(start = 8.dp)) {
            text()
        }
    }
}

@Composable
public fun ToolBarActionIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TooltipArea(
        modifier = modifier,
        tooltip = {
            Card(elevation = 4.dp) {
                Text(contentDescription)
            }
        },
        content = {
            IconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = 1.dp,
                        MaterialTheme.colors.onSurface.copy(alpha = 0.38f),
                    )
            ) { Icon(imageVector, contentDescription) }
        },
    )
}

@Composable
public fun <T> DropdownSelector(
    items: List<T>,
    value: T,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    valueRender: (T) -> String = { it.toString() },
    label: @Composable (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester.Default }
    var focusState: FocusState? by remember { mutableStateOf(null) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = valueRender(value),
            label = label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { focusRequester.requestFocus() }) {
                    Icon(Icons.Default.ArrowDropDown, "select")
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState = it }
        )
        DropdownMenu(
            expanded = focusState?.isFocused == true,
            onDismissRequest = { focusManager.clearFocus() },
        ) {
            items.forEach { item: T ->
                DropdownMenuItem(
                    onClick = {
                        onValueChange(item)
                        focusManager.clearFocus()
                    }
                ) {
                    Text(text = valueRender(item))
                }
            }
        }
    }
}

@Composable
public fun <T> RadioGroup(
    items: List<T>,
    value: T,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    valueRender: (T) -> String = { it.toString() },
    label: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        label?.invoke()

        items.forEach { item: T ->
            RadioButtonArea(
                selected = item == value,
                onClick = { onValueChange(item) },
                text = { Text(text = valueRender(item)) },
            )
        }
    }
}

@Composable
public fun <T> viewModelValue(calculation: () -> T): State<T> {
    return derivedStateOf { calculation() }
//    return remember { derivedStateOf { calculation() } }
}

public fun String.asContentType(): ContentType {
    return ContentType.parse(this)
}

public fun ContentType.asContentTypeString(): String {
    return "$contentType/$contentSubtype"
}
