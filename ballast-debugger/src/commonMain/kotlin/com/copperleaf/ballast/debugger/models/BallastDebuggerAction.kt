package com.copperleaf.ballast.debugger.models

import kotlinx.serialization.Serializable

@Serializable
public sealed class BallastDebuggerAction {

    public abstract val connectionId: String

    @Serializable
    public data class RequestViewModelRefresh(
        override val connectionId: String,
        val viewModelName: String,
    ) : BallastDebuggerAction()
}
