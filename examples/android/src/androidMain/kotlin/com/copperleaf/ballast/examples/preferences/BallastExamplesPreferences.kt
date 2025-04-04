package com.copperleaf.ballast.examples.preferences

import com.copperleaf.ballast.examples.ui.kitchensink.InputStrategySelection

interface BallastExamplesPreferences {
    // kitchen sink
    var kitchenSinkInputStrategySelection: InputStrategySelection

    // scorekeeper
    var scorekeeperButtonValues: List<Int>
    var scorekeeperScoresheetState: Map<String, Int>
}
