package com.copperleaf.ballast.debugger.server.versions.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val ACTION_MODEL_BASE_CLASS_NAME = "com.copperleaf.ballast.debugger.models.BallastDebuggerAction"

@Serializable
public sealed class BallastDebuggerActionV2 {

    public abstract val connectionId: String
    public abstract val viewModelName: String

    @Serializable
    @SerialName("$ACTION_MODEL_BASE_CLASS_NAME.RequestViewModelRefresh")
    public data class RequestViewModelRefresh(
        override val connectionId: String,
        override val viewModelName: String,
    ) : BallastDebuggerActionV2()

    @Serializable
    @SerialName("$ACTION_MODEL_BASE_CLASS_NAME.RequestRestoreState")
    public data class RequestRestoreState(
        override val connectionId: String,
        override val viewModelName: String,
        val stateUuid: String,
    ) : BallastDebuggerActionV2()

    @Serializable
    @SerialName("$ACTION_MODEL_BASE_CLASS_NAME.RequestResendInput")
    public data class RequestResendInput(
        override val connectionId: String,
        override val viewModelName: String,
        val inputUuid: String,
    ) : BallastDebuggerActionV2()
}
