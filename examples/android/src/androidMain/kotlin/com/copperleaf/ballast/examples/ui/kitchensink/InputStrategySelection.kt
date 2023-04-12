package com.copperleaf.ballast.examples.ui.kitchensink

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.ParallelInputStrategy

enum class InputStrategySelection(
    val get: () -> InputStrategy<*, *, *>
) {
    Lifo({ LifoInputStrategy() }),
    Fifo({ FifoInputStrategy() }),
    Parallel({ ParallelInputStrategy() }),
}
