package com.copperleaf.ballast.examples.kitchensink

import com.copperleaf.ballast.BallastViewModel

expect class KitchenSinkViewModel : BallastViewModel<
    KitchenSinkContract.Inputs,
    KitchenSinkContract.Events,
    KitchenSinkContract.State>
