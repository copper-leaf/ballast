package com.copperleaf.ballast.debugger.server.versions.unsupported

import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.server.ClientModelMapper

class ClientModelMapperUnsupportedVersion(val version: String) : ClientModelMapper {
    override val supported: Boolean = false

    override fun mapIncoming(incoming: String): BallastDebuggerEvent {
        throw NotImplementedError("Ballast client version '$version' is not supported")
    }

    override fun mapOutgoing(outgoing: BallastDebuggerAction): String {
        throw NotImplementedError("Ballast client version '$version' is not supported")
    }
}
