package com.copperleaf.ballast.debugger.idea.ui.debugger.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Badge
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
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
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.idea.ui.debugger.BallastDebuggerUiSettings
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRoute
import com.copperleaf.ballast.debugger.idea.utils.maybeFilter
import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.models.BallastConnectionState
import com.copperleaf.ballast.debugger.models.BallastEventState
import com.copperleaf.ballast.debugger.models.BallastInputState
import com.copperleaf.ballast.debugger.models.BallastSideJobState
import com.copperleaf.ballast.debugger.models.BallastStateSnapshot
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.now
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.optionalStringPath
import com.copperleaf.ballast.navigation.routing.pathParameter
import com.copperleaf.ballast.navigation.routing.stringPath
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
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

val LocalProject = compositionLocalOf<Project> { error("LocalProject not provided") }
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

@Composable
fun rememberConnectionsList(
    serverState: BallastApplicationState,
): List<BallastConnectionState> {
    return remember(serverState) {
        serverState.connections
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedConnection(
    connectionsList: List<BallastConnectionState>,
): BallastConnectionState? {
    val connectionId: String? by optionalStringPath()
    return remember(connectionsList, connectionId) {
        connectionsList.find { connection -> connection.connectionId == connectionId }
    }
}

@Composable
fun rememberViewModelList(
    connection: BallastConnectionState?,
): List<BallastViewModelState?> {
    return remember(connection) {
        if (connection == null) {
            emptyList()
        } else {
            listOf(null) + connection.viewModels
        }
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedViewModel(
    connection: BallastConnectionState?,
): BallastViewModelState? {
    val viewModelName: String by stringPath()
    return remember(viewModelName) {
        connection?.viewModels?.find { vm -> vm.viewModelName == viewModelName }
    }
}

@Composable
fun rememberViewModelStatesList(
    viewModel: BallastViewModelState?,
    searchText: String,
): List<BallastStateSnapshot> {
    return remember(viewModel, searchText) {
        viewModel?.states?.maybeFilter(searchText) {
            listOf(it.toStringValue)
        } ?: emptyList()
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedViewModelStateSnapshot(
    viewModel: BallastViewModelState?,
): BallastStateSnapshot? {
    val stateUuid: String by stringPath()
    return remember(stateUuid) {
        viewModel?.states?.find { state -> state.uuid == stateUuid }
    }
}

@Composable
fun rememberViewModelInputsList(
    viewModel: BallastViewModelState?,
    searchText: String,
): List<BallastInputState> {
    return remember(viewModel, searchText) {
        viewModel?.inputs?.maybeFilter(searchText) {
            listOf(it.type, it.toStringValue)
        } ?: emptyList()
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedViewModelInput(
    viewModel: BallastViewModelState?,
): BallastInputState? {
    val inputUuid: String by stringPath()
    return remember(inputUuid) {
        viewModel?.inputs?.find { it.uuid == inputUuid }
    }
}

@Composable
fun rememberViewModelEventsList(
    viewModel: BallastViewModelState?,
    searchText: String,
): List<BallastEventState> {
    return remember(viewModel, searchText) {
        viewModel?.events?.maybeFilter(searchText) {
            listOf(it.type, it.toStringValue)
        } ?: emptyList()
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedViewModelEvent(
    viewModel: BallastViewModelState?,
): BallastEventState? {
    val eventUuid: String by stringPath()
    return remember(eventUuid) {
        viewModel?.events?.find { it.uuid == eventUuid }
    }
}

@Composable
fun rememberViewModelSideJobsList(
    viewModel: BallastViewModelState?,
    searchText: String,
): List<BallastSideJobState> {
    return remember(viewModel, searchText) {
        viewModel?.sideJobs?.maybeFilter(searchText) {
            listOf(it.key)
        } ?: emptyList()
    }
}

@Composable
fun Destination.ParametersProvider.rememberSelectedViewModelSideJob(
    viewModel: BallastViewModelState?,
): BallastSideJobState? {
    val sideJobUuid: String by stringPath()
    return remember(sideJobUuid) {
        viewModel?.sideJobs?.find { it.uuid == sideJobUuid }
    }
}

@Composable
fun rememberConnectionCurrentDestination(
    connection: BallastConnectionState?,
    uiSettings: BallastDebuggerUiSettings,
): String? {
    return remember(connection, uiSettings) {
        if (!uiSettings.showCurrentRoute) {
            null
        } else {
            connection
                ?.viewModels
                ?.find { it.viewModelName == uiSettings.routerViewModelName }
                ?.states
                ?.firstOrNull()
                ?.toStringValue
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

fun getRouteForSelectedViewModel(
    currentRoute: DebuggerRoute,
    connectionId: String,
    viewModelName: String?,
): String {
    return if (viewModelName != null) {
        val newRoute = when (currentRoute) {
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
            DebuggerRoute.ViewModelTimeline -> DebuggerRoute.ViewModelTimeline
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
fun CheckboxArea(
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
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
        )
        content()
    }
}

@Composable
fun ToolBarActionIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    TooltipArea(
        tooltip = {
            Card(elevation = 4.dp) {
                Text(contentDescription)
            }
        },
        content = {
            IconButton(
                onClick = onClick,
            ) { Icon(imageVector, contentDescription) }
        },
    )
}

@Composable
fun <T> DropdownSelector(
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