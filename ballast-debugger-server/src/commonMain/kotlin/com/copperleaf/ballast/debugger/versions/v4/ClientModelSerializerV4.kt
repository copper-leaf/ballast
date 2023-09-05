package com.copperleaf.ballast.debugger.versions.v4

import com.copperleaf.ballast.debugger.versions.ClientModelSerializer
import kotlinx.serialization.json.Json

public class ClientModelSerializerV4 : ClientModelSerializer<BallastDebuggerEventV4, BallastDebuggerActionV4> {
    private val debuggerEventJson: Json = Json {
        isLenient = true
    }

    override val supported: Boolean = true

    override fun mapIncoming(incoming: String): BallastDebuggerEventV4 {
        return debuggerEventJson
            .decodeFromString(BallastDebuggerEventV4.serializer(), incoming)
    }

    override fun mapOutgoing(outgoing: BallastDebuggerActionV4): String {
        return debuggerEventJson
            .encodeToString(BallastDebuggerActionV4.serializer(), outgoing)
    }
}
