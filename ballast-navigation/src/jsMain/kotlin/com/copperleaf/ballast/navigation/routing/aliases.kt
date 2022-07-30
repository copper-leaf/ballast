package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import kotlinx.coroutines.flow.Flow

internal typealias RouterInterceptor = BallastInterceptor<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State,
    >

internal typealias RouterInterceptorScope = BallastInterceptorScope<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State,
    >

internal typealias RouterNotifications = Flow<BallastNotification<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State,
    >>
