package com.copperleaf.ballast.debugger.ui.widgets

import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

fun LocalDateTime.format(pattern: String): String {
    return this.toJavaLocalDateTime().format(
        DateTimeFormatter.ofPattern(pattern)
    )
}

fun LocalDateTime.ago(now: LocalDateTime = LocalDateTime.now()): Duration {
    return now - this
}
