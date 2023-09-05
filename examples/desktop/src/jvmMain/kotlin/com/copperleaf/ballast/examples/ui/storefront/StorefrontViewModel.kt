package com.copperleaf.ballast.examples.ui.storefront

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.eventHandler
import kotlinx.coroutines.CoroutineScope

class StorefrontViewModel(
    viewModelCoroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
            StorefrontContract.Inputs,
            StorefrontContract.Events,
            StorefrontContract.State>,
) : BasicViewModel<
        StorefrontContract.Inputs,
        StorefrontContract.Events,
        StorefrontContract.State>(
    config = config,
    eventHandler = eventHandler { },
    coroutineScope = viewModelCoroutineScope,
)
