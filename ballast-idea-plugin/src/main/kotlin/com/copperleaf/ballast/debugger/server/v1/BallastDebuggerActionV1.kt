package com.copperleaf.ballast.debugger.server.v1

import kotlinx.serialization.Serializable

@Serializable
public sealed class BallastDebuggerActionV1 {

    public abstract val connectionId: String
    public abstract val viewModelName: String

    @Serializable
    public data class RequestViewModelRefresh(
        override val connectionId: String,
        override val viewModelName: String,
    ) : BallastDebuggerActionV1()

    @Serializable
    public data class RequestRestoreState(
        override val connectionId: String,
        override val viewModelName: String,
        val stateUuid: String,
    ) : BallastDebuggerActionV1()

    @Serializable
    public data class RequestResendInput(
        override val connectionId: String,
        override val viewModelName: String,
        val inputUuid: String,
    ) : BallastDebuggerActionV1()
}
