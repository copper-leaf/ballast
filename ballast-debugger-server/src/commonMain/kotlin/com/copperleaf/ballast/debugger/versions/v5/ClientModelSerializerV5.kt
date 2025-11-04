package com.copperleaf.ballast.debugger.versions.v5

import com.copperleaf.ballast.debugger.versions.ClientModelSerializer
import kotlinx.serialization.json.Json

public class ClientModelSerializerV5 : ClientModelSerializer<BallastDebuggerEventV5, BallastDebuggerActionV5> {
    private val debuggerEventJson: Json = Json {
        isLenient = true
    }

    override val supported: Boolean = true

    override fun mapIncoming(incoming: String): BallastDebuggerEventV5 {
        return debuggerEventJson
            .decodeFromString(BallastDebuggerEventV5.serializer(), incoming)
    }

    override fun mapOutgoing(outgoing: BallastDebuggerActionV5): String {
        return debuggerEventJson
            .encodeToString(BallastDebuggerActionV5.serializer(), outgoing)
    }
}
