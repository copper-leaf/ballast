package com.copperleaf.ballast

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>.withSerialization(
    inputsSerializer: KSerializer<Inputs>,
    eventsSerializer: KSerializer<Events>,
    stateSerializer: KSerializer<State>,
    json: Json = Json { prettyPrint = true },
): BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State> = this.apply {
    val encoderDecoder = JsonBallastEncoder(
        inputsSerializer = inputsSerializer,
        eventsSerializer = eventsSerializer,
        stateSerializer = stateSerializer,
        json = json,
    )
    this.encoder = encoderDecoder
    this.decoder = encoderDecoder
}
