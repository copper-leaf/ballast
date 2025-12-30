package com.copperleaf.ballast.autoscale

import com.copperleaf.ballast.BallastViewModel

public fun interface DistributionPolicy<Inputs : Any, Events : Any, State : Any> {
    public fun getPolicyState(): PolicyState<Inputs, Events, State>

    public fun interface PolicyState<Inputs : Any, Events : Any, State : Any> {
        public fun getNextViewModel(
            pool: List<BallastViewModel<Inputs, Events, State>>
        ): BallastViewModel<Inputs, Events, State>?
    }
}
