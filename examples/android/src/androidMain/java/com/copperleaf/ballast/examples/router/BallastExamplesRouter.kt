package com.copperleaf.ballast.examples.router

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.vm.Router
import kotlinx.coroutines.CoroutineScope

class BallastExamplesRouter(
    config: BallastViewModelConfiguration<
        RouterContract.Inputs<BallastExamples>,
        RouterContract.Events<BallastExamples>,
        RouterContract.State<BallastExamples>>,
    coroutineScope: CoroutineScope,
) : AndroidViewModel<
    RouterContract.Inputs<BallastExamples>,
    RouterContract.Events<BallastExamples>,
    RouterContract.State<BallastExamples>>(
    config = config,
    coroutineScope = coroutineScope,
), Router<BallastExamples>
