package com.copperleaf.ballast.debugger.versions

public open class CompositeModelConverter<
        EventVersionA,
        EventVersionB,
        EventVersionC,
        ActionVersionA,
        ActionVersionB,
        ActionVersionC
        >(
    private val converter1To2: ClientModelConverter<
            EventVersionA,
            EventVersionB,
            ActionVersionA,
            ActionVersionB
            >,
    private val converter2To3: ClientModelConverter<
            EventVersionB,
            EventVersionC,
            ActionVersionB,
            ActionVersionC
            >,
) : ClientModelConverter<
        EventVersionA,
        EventVersionC,
        ActionVersionA,
        ActionVersionC
        > {

    override fun mapEvent(event: EventVersionA): EventVersionC {
        val eventA: EventVersionA = event
        val eventB: EventVersionB = converter1To2.mapEvent(eventA)
        val eventC: EventVersionC = converter2To3.mapEvent(eventB)
        return eventC
    }

    override fun mapAction(action: ActionVersionC): ActionVersionA {
        val actionC: ActionVersionC = action
        val actionB: ActionVersionB = converter2To3.mapAction(actionC)
        val actionA: ActionVersionA = converter1To2.mapAction(actionB)
        return actionA
    }
}
