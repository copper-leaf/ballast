package com.copperleaf.ballast.debugger.versions.v2

import com.copperleaf.ballast.debugger.versions.ClientModelSerializer
import kotlinx.serialization.json.Json

class ClientModelSerializerV2 : ClientModelSerializer<BallastDebuggerEventV2, BallastDebuggerActionV2> {
    private val debuggerEventJson: Json = Json {
        isLenient = true
    }

    override val supported: Boolean = true

    override fun mapIncoming(incoming: String): BallastDebuggerEventV2 {
        return debuggerEventJson
            .decodeFromString(BallastDebuggerEventV2.serializer(), incoming)
    }

    override fun mapOutgoing(outgoing: BallastDebuggerActionV2): String {
        return debuggerEventJson
            .encodeToString(BallastDebuggerActionV2.serializer(), outgoing)
    }
}
