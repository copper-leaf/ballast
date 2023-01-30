package com.copperleaf.ballast.debugger.versions

interface ClientModelConverter<
        EventLowerVersion,
        EventHigherVersion,
        ActionLowerVersion,
        ActionHigherVersion
        > {

    fun mapEvent(event: EventLowerVersion): EventHigherVersion

    fun mapAction(action: ActionHigherVersion): ActionLowerVersion
}
