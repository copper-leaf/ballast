package com.copperleaf.ballast.examples.router

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.vm.Router
import kotlinx.coroutines.CoroutineScope

class BallastExamplesRouter(
    viewModelCoroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
        RouterContract.Inputs<BallastExamples>,
        RouterContract.Events<BallastExamples>,
        RouterContract.State<BallastExamples>>,
) : BasicViewModel<
    RouterContract.Inputs<BallastExamples>,
    RouterContract.Events<BallastExamples>,
    RouterContract.State<BallastExamples>>(
    config = config,
    eventHandler = eventHandler { },
    coroutineScope = viewModelCoroutineScope,
), Router<BallastExamples>
