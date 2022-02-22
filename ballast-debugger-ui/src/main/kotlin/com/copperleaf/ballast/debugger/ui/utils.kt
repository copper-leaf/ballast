package com.copperleaf.ballast.debugger.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.debugger.models.minus
import com.copperleaf.ballast.debugger.models.now
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
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

@Composable
fun currentTimeAsState(): State<LocalDateTime> {
    return produceState(LocalDateTime.now()) {
        while (true) {
            delay(250)
            value = LocalDateTime.now()
        }
    }
}

fun Duration.round(): Duration {
    return when {
        inWholeDays > 0 -> Duration.Companion.days(inWholeDays)
        inWholeHours > 0 -> Duration.Companion.hours(inWholeHours)
        inWholeMinutes > 0 -> Duration.Companion.minutes(inWholeMinutes)
        else -> Duration.Companion.seconds(inWholeSeconds)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusIcon(
    isActive: Boolean,
    activeText: String,
    inactiveText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val text = if (isActive) {
        activeText
    } else {
        inactiveText
    }
    val tint = if (isActive) {
        MaterialTheme.colors.primary
    } else {
        null
    }

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
            Card {
                if (tint != null) {
                    Icon(icon, text, tint = tint)
                } else {
                    Icon(icon, text)
                }
            }
        }
    )
}
