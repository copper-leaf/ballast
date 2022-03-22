package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputStrategy
import kotlinx.coroutines.CoroutineDispatcher

public class DefaultViewModelConfiguration<Inputs : Any, Events : Any, State : Any>(
    override val initialState: State,
    override val inputHandler: InputHandler<Inputs, Events, State>,
    override val filter: InputFilter<Inputs, Events, State>?,
    override val interceptors: List<BallastInterceptor<Inputs, Events, State>>,
    override val inputStrategy: InputStrategy,
    override val inputsDispatcher: CoroutineDispatcher,
    override val eventsDispatcher: CoroutineDispatcher,
    override val sideJobsDispatcher: CoroutineDispatcher,
    override val interceptorDispatcher: CoroutineDispatcher,
    override val name: String,
    override val logger: BallastLogger,
) : BallastViewModelConfiguration<Inputs, Events, State>
