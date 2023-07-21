package com.copperleaf.ballast.contracts.dispatcher

import com.copperleaf.ballast.BallastViewModel

public typealias DispatcherTestViewModel = BallastViewModel<
        DispatcherTestContract.Inputs,
        DispatcherTestContract.Events,
        DispatcherTestContract.State>
