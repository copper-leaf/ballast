package com.copperleaf.ballast.debugger.models

import com.copperleaf.ballast.debugger.utils.minus
import com.copperleaf.ballast.debugger.utils.now
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration.Companion.seconds

public data class BallastConnectionState(
    public val connectionId: String,
    public val connectionBallastVersion: String = "",
    public val viewModels: List<BallastViewModelState> = emptyList(),
    public val firstSeen: LocalDateTime = LocalDateTime.now(),
    public val lastSeen: LocalDateTime = LocalDateTime.now(),
) {
    public fun isActive(currentTime: LocalDateTime): Boolean {
        return (currentTime - lastSeen) <= 10.seconds
    }

    public fun updateViewModel(
        viewModelName: String?,
        block: BallastViewModelState.() -> BallastViewModelState,
    ): BallastConnectionState {
        val indexOfViewModel = viewModels.indexOfFirst { it.viewModelName == viewModelName }

        return this.copy(
            viewModels = viewModels
                .toMutableList()
                .apply {
                    if (viewModelName != null) {
                        if (indexOfViewModel != -1) {
                            // we're updating a value in an existing connection
                            this[indexOfViewModel] = this[indexOfViewModel].block().copy(lastSeen = LocalDateTime.now())
                        } else {
                            // this is the first time we're seeing this connection, create a new entry for it
                            this.add(
                                0,
                                BallastViewModelState(connectionId, viewModelName, firstSeen = LocalDateTime.now()).block()
                            )
                        }
                    }
                }
                .toList()
        )
    }
}
