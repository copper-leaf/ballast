package com.copperleaf.ballast.debugger.server.v2

import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.server.ClientModelMapper
import kotlinx.serialization.json.Json

class ClientModelMapperV2 : ClientModelMapper {
    private val debuggerEventJson: Json = Json {
        isLenient = true
    }

// Incoming
// ---------------------------------------------------------------------------------------------------------------------

    override fun mapIncoming(incoming: String): BallastDebuggerEvent {
        return debuggerEventJson
            .decodeFromString(BallastDebuggerEvent.serializer(), incoming)
    }

// Outgoing
// ---------------------------------------------------------------------------------------------------------------------

    override fun mapOutgoing(outgoing: BallastDebuggerAction): String {
        return debuggerEventJson
            .encodeToString(BallastDebuggerAction.serializer(), outgoing)
    }
}
