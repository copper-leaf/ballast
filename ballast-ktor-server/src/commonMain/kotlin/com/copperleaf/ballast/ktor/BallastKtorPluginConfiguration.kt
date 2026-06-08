package com.copperleaf.ballast.ktor

import com.copperleaf.ballast.BallastViewModel
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope

@Suppress("UNCHECKED_CAST")
public class BallastKtorPluginConfiguration {
    internal var viewModels: MutableList<RegisteredViewModel<*, *, *>> = mutableListOf()

    public fun <VM : BallastViewModel<Inputs, Events, State>, Inputs : Any, Events : Any, State : Any> viewModel(
        attributeKey: AttributeKey<VM>,
        createViewModel: (CoroutineScope) -> VM,
    ) {
        viewModels += RegisteredViewModel(
            attributeKey = attributeKey as AttributeKey<BallastViewModel<Inputs, Events, State>>,
            createViewModel = createViewModel
        )
    }
}
