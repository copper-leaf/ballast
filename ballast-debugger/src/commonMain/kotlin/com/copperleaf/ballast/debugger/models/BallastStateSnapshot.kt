package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime

public data class BallastStateSnapshot(
    public val connectionId: String,
    public val viewModelName: String,
    public val uuid: String,
    public val actualState: Any?,

    public val type: String = "",
    public val serializedValue: String = "",
    public val contentType: String = "",

    public val emittedAt: LocalDateTime = LocalDateTime.now(),
)
