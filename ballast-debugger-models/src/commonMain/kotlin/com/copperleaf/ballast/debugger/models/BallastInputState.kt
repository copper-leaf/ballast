package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration

public data class BallastInputState(
    public val connectionId: String,
    public val viewModelName: String,
    public val uuid: String,
    public val actualInput: Any?,

    public val type: String = "",
    public val serializedValue: String = "",
    public val contentType: String = "",

    public val status: Status = Status.Queued,

    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
) {
    public sealed class Status {
        public object Queued : Status() {
            override fun toString(): String = "Queued"
        }

        public object Dropped : Status() {
            override fun toString(): String = "Dropped"
        }

        public object Running : Status() {
            override fun toString(): String = "Running"
        }

        public object Rejected : Status() {
            override fun toString(): String = "Rejected"
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
