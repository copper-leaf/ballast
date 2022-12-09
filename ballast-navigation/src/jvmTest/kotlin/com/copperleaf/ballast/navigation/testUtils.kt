package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.test.BallastTestSuiteScope
import com.copperleaf.ballast.test.viewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
fun <Inputs : Any, Events : Any, State : Any> runBlockingViewModelTest(
    inputHandler: InputHandler<Inputs, Events, State>,
    eventHandler: EventHandler<Inputs, Events, State>,
    filter: InputFilter<Inputs, Events, State>? = null,
    block: BallastTestSuiteScope<Inputs, Events, State>.() -> Unit
) = runBlocking<Unit> {
    viewModelTest(
        inputHandler = inputHandler,
        eventHandler = eventHandler,
        filter = filter,
        block = block,
    )
}
