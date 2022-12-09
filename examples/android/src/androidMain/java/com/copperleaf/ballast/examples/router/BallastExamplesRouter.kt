package com.copperleaf.ballast.examples.router

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.vm.Router

class BallastExamplesRouter(
    config: BallastViewModelConfiguration<
        RouterContract.Inputs<BallastExamples>,
        RouterContract.Events<BallastExamples>,
        RouterContract.State<BallastExamples>>,
) : AndroidViewModel<
    RouterContract.Inputs<BallastExamples>,
    RouterContract.Events<BallastExamples>,
    RouterContract.State<BallastExamples>>(
    config = config,
), Router<BallastExamples>
