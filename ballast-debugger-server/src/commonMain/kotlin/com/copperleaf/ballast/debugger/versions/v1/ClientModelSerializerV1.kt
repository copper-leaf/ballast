package com.copperleaf.ballast.debugger.versions.v1

import com.copperleaf.ballast.debugger.versions.ClientModelSerializer
import kotlinx.serialization.json.Json

public class ClientModelSerializerV1 : ClientModelSerializer<BallastDebuggerEventV1, BallastDebuggerActionV1> {
    private val debuggerEventJson: Json = Json {
        isLenient = true
    }

    override val supported: Boolean = true

    override fun mapIncoming(incoming: String): BallastDebuggerEventV1 {
        return debuggerEventJson
            .decodeFromString(BallastDebuggerEventV1.serializer(), incoming)
    }

    override fun mapOutgoing(outgoing: BallastDebuggerActionV1): String {
        return debuggerEventJson
            .encodeToString(BallastDebuggerActionV1.serializer(), outgoing)
    }
}
