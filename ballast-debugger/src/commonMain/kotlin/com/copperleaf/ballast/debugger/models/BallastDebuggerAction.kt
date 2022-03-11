package com.copperleaf.ballast.debugger.models

import kotlinx.serialization.Serializable

@Serializable
public sealed class BallastDebuggerAction {

    public abstract val connectionId: String
    public abstract val viewModelName: String

    @Serializable
    public data class RequestViewModelRefresh(
        override val connectionId: String,
        override val viewModelName: String,
    ) : BallastDebuggerAction()

    @Serializable
    public data class RequestRestoreState(
        override val connectionId: String,
        override val viewModelName: String,
        val stateUuid: String,
    ) : BallastDebuggerAction()

    @Serializable
    public data class RequestResendInput(
        override val connectionId: String,
        override val viewModelName: String,
        val inputUuid: String,
    ) : BallastDebuggerAction()
}
