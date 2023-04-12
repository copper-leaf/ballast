package com.copperleaf.ballast.debugger.versions

public interface ClientModelConverter<
        EventLowerVersion,
        EventHigherVersion,
        ActionLowerVersion,
        ActionHigherVersion
        > {

    public fun mapEvent(event: EventLowerVersion): EventHigherVersion

    public fun mapAction(action: ActionHigherVersion): ActionLowerVersion
}
