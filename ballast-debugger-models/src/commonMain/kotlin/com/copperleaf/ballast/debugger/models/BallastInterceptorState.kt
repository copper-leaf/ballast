package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime

public data class BallastInterceptorState(
    public val connectionId: String,
    public val viewModelName: String,
    public val uuid: String,

    public val type: String = "",
    public val toStringValue: String = "",

    public val status: BallastInterceptorState.Status = BallastInterceptorState.Status.Attached,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
) {
    public enum class Status { Attached, Failed }
}
