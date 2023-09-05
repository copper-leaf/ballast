package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

public data class BallastSideJobState(
    public val connectionId: String,
    public val viewModelName: String,
    public val uuid: String,

    public val key: String = "",
    public val restartState: SideJobScope.RestartState = SideJobScope.RestartState.Initial,
    public val status: Status = Status.Running,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
) {
    public sealed class Status {
        public object Queued : Status() {
            override fun toString(): String = "Queued"
        }

        public object Running : Status() {
            override fun toString(): String = "Running"
        }

        public data class Cancelled(val duration: Duration) : Status() {
            override fun toString(): String = "Cancelled after $duration"
        }

        public data class Error(val duration: Duration, val stacktrace: String) : Status() {
            override fun toString(): String = "Failed after $duration"
        }

        public data class Completed(val duration: Duration) : Status() {
            override fun toString(): String = "Completed after $duration"
        }
    }
}
