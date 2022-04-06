package com.copperleaf.ballast.android.kitchensink

import com.copperleaf.ballast.android.util.commonBuilder
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkContract
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkInputHandler
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class KitchenSinkViewModel : AndroidViewModel<
    KitchenSinkContract.Inputs,
    KitchenSinkContract.Events,
    KitchenSinkContract.State>(
    config = commonBuilder()
        .forViewModel(
            initialState = KitchenSinkContract.State(),
            inputHandler = KitchenSinkInputHandler(),
            name = "KitchenSink",
        ),
)
