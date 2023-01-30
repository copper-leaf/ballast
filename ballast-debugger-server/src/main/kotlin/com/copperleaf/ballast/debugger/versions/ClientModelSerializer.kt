package com.copperleaf.ballast.debugger.versions

interface ClientModelSerializer<Event, Action> {

    val supported: Boolean

    fun mapIncoming(incoming: String): Event

    fun mapOutgoing(outgoing: Action): String
}
