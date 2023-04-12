package com.copperleaf.ballast.debugger.versions

public interface ClientModelSerializer<Event, Action> {

    public val supported: Boolean

    public fun mapIncoming(incoming: String): Event

    public fun mapOutgoing(outgoing: Action): String
}
