package com.copperleaf.ballast.examples.preferences

import com.copperleaf.ballast.examples.ui.kitchensink.InputStrategySelection
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class BallastExamplesPreferencesImpl(
    private val settings: Settings,
) : BallastExamplesPreferences {

// KitchenSink
// ---------------------------------------------------------------------------------------------------------------------
    private val kitchenSinkInputStrategySelectionKey = "KitchenSink.inputStrategySelection"
    override var kitchenSinkInputStrategySelection: InputStrategySelection
        get() {
            return settings.getStringOrNull(kitchenSinkInputStrategySelectionKey)
                ?.let { InputStrategySelection.valueOf(it) }
                ?: InputStrategySelection.Lifo
        }
        set(value) {
            settings[kitchenSinkInputStrategySelectionKey] = value.name
        }

// Scorekeeper
// ---------------------------------------------------------------------------------------------------------------------

    private val scorekeeperButtonValuesKey = "ScoreKeeper.buttonValues"
    private val scorekeeperButtonValuesSerializer = ListSerializer(Int.serializer())
    override var scorekeeperButtonValues: List<Int>
        get() {
            val buttonValuesJsonString: String = settings[scorekeeperButtonValuesKey] ?: "[]"
            return Json.decodeFromString(scorekeeperButtonValuesSerializer, buttonValuesJsonString)
        }
        set(value) {
            val buttonValuesJsonString = Json.encodeToString(scorekeeperButtonValuesSerializer, value)
            settings[scorekeeperButtonValuesKey] = buttonValuesJsonString
        }

    private val scorekeeperScoresheetStateKey = "ScoreKeeper.scoresheetState"
    private val scorekeeperScoresheetStateSerializer = MapSerializer(String.serializer(), Int.serializer())
    override var scorekeeperScoresheetState: Map<String, Int>
        get() {
            val buttonValuesJsonString: String = settings[scorekeeperScoresheetStateKey] ?: "{}"
            return Json.decodeFromString(scorekeeperScoresheetStateSerializer, buttonValuesJsonString)
        }
        set(value) {
            val buttonValuesJsonString = Json.encodeToString(scorekeeperScoresheetStateSerializer, value)
            settings[scorekeeperScoresheetStateKey] = buttonValuesJsonString
        }

// Router
// ---------------------------------------------------------------------------------------------------------------------

    private val backstackStateKey = "Router.backstack"
    private val backstackStateSerializer = ListSerializer(String.serializer())
    override var backstack: List<String>
        get() {
            val backstackUrlsString: String = settings[backstackStateKey] ?: "[]"
            return Json.decodeFromString(backstackStateSerializer, backstackUrlsString)
        }
        set(value) {
            val backstackUrlsString = Json.encodeToString(backstackStateSerializer, value)
            settings[backstackStateKey] = backstackUrlsString
        }
}
