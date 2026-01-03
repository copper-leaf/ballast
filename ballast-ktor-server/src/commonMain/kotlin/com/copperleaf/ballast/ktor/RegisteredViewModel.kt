package com.copperleaf.ballast.ktor

import com.copperleaf.ballast.BallastViewModel
import io.ktor.server.application.Application
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope

public data class RegisteredViewModel<Inputs : Any, Events : Any, State : Any>(
    val attributeKey: AttributeKey<BallastViewModel<Inputs, Events, State>>,
    val createViewModel: (CoroutineScope) -> BallastViewModel<Inputs, Events, State>,
) {
    private lateinit var vm: BallastViewModel<Inputs, Events, State>
    internal fun startProcessing(application: Application, coroutineScope: CoroutineScope) {
        vm = createViewModel(coroutineScope)
        application.attributes.put(attributeKey, vm)
    }

    internal suspend fun shutDownGracefully() {
        vm.close()
    }
}
