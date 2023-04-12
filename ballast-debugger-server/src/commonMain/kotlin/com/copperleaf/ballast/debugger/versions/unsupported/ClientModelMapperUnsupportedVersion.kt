package com.copperleaf.ballast.debugger.versions.unsupported

import com.copperleaf.ballast.debugger.versions.ClientModelSerializer
import com.copperleaf.ballast.debugger.versions.ClientVersion

public class ClientModelMapperUnsupportedVersion<EventT, ActionT>(
    private val version: ClientVersion
) : ClientModelSerializer<EventT, ActionT> {

    override val supported: Boolean = false

    override fun mapIncoming(incoming: String): EventT {
        throw NotImplementedError("Ballast client version '$version' is not supported")
    }

    override fun mapOutgoing(outgoing: ActionT): String {
        throw NotImplementedError("Ballast client version '$version' is not supported")
    }
}
