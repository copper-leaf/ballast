package com.copperleaf.ballast.analytics

import com.copperleaf.ballast.BallastEncoder

/**
 * A default [AnalyticsAdapter] implementation that collects basic information about each Input and tracks them with
 * an `eventId` of "action". You must provide a `shouldTrackInput
 */
public class DefaultAnalyticsAdapter<Inputs : Any, Events : Any, State : Any>(
    shouldTrackInput: (Inputs) -> Boolean = { true },
) : AnalyticsAdapter<Inputs, Events, State> {
    private val shouldTrackInputFn: (Inputs) -> Boolean = shouldTrackInput

    override fun shouldTrackInput(input: Inputs): Boolean {
        return shouldTrackInputFn(input)
    }

    override fun getEventIdForInput(input: Inputs): String {
        return "action"
    }

    override fun getEventParametersForInput(
        viewModelName: String,
        input: Inputs,
        encoder: BallastEncoder<Inputs, Events, State>
    ): Map<String, String> {
        return mapOf(
            Keys.ViewModelName to viewModelName,
            Keys.InputType to "$viewModelName.${input::class.simpleName}",
            Keys.InputValue to "$viewModelName.${encoder.encodeInputToString(input)}",
        )
    }

    private object Keys {
        const val ViewModelName = "ViewModelName"
        const val InputType = "InputType"
        const val InputValue = "InputValue"
    }
}
