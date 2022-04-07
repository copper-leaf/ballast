package com.copperleaf.ballast.examples.util

import com.copperleaf.ballast.examples.kitchensink.controller.InputStrategySelection

interface ExamplesPreferences {
    // kitchen sink
    var kitchenSinkInputStrategySelection: InputStrategySelection

    // scorekeeper
    var scorekeeperButtonValues: List<Int>
    var scorekeeperScoresheetState: Map<String, Int>
}
