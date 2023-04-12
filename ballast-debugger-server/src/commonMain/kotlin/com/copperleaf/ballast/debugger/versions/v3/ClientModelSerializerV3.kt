package com.copperleaf.ballast.debugger.versions.v3

import com.copperleaf.ballast.debugger.versions.ClientModelSerializer
import kotlinx.serialization.json.Json

public class ClientModelSerializerV3 : ClientModelSerializer<BallastDebuggerEventV3, BallastDebuggerActionV3> {
    private val debuggerEventJson: Json = Json {
        isLenient = true
    }

    override val supported: Boolean = true

    override fun mapIncoming(incoming: String): BallastDebuggerEventV3 {
        return debuggerEventJson
            .decodeFromString(BallastDebuggerEventV3.serializer(), incoming)
    }

    override fun mapOutgoing(outgoing: BallastDebuggerActionV3): String {
        return debuggerEventJson
            .encodeToString(BallastDebuggerActionV3.serializer(), outgoing)
    }
}
