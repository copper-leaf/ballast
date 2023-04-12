package com.copperleaf.ballast.debugger.versions

public open class CompositeModelSerializer<
        EventLowerVersion,
        EventHigherVersion,
        ActionLowerVersion,
        ActionHigherVersion
        >(
    private val serializer: ClientModelSerializer<EventLowerVersion, ActionLowerVersion>,
    private val converter: ClientModelConverter<EventLowerVersion,
            EventHigherVersion,
            ActionLowerVersion,
            ActionHigherVersion>,
) : ClientModelSerializer<EventHigherVersion, ActionHigherVersion> {

    override val supported: Boolean = serializer.supported

    override fun mapIncoming(incoming: String): EventHigherVersion {
        val eventAtLowerVersion: EventLowerVersion = serializer.mapIncoming(incoming)
        val eventAtHigherVersion: EventHigherVersion = converter.mapEvent(eventAtLowerVersion)
        return eventAtHigherVersion
    }

    override fun mapOutgoing(outgoing: ActionHigherVersion): String {
        val actionAtLowerVersion: ActionLowerVersion = converter.mapAction(outgoing)
        val serializedToLowerVersion: String = serializer.mapOutgoing(actionAtLowerVersion)
        return serializedToLowerVersion
    }
}
