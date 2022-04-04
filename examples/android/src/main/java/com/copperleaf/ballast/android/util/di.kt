@file:OptIn(ExperimentalTime::class)

package com.copperleaf.ballast.android.util

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidBallastLogger
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.plusAssign
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.time.ExperimentalTime

private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
private val httpClient = HttpClient(OkHttp)
private val debuggerConnection by lazy {
    BallastDebuggerClientConnection(OkHttp, applicationScope).also { it.connect() }
}

//private val eventBus = EventBusImpl()
//private val bggApi = BggApiImpl(httpClient)
//private val bggRepository by lazy {
//    BggRepositoryImpl(
//        coroutineScope = applicationScope,
//        eventBus = eventBus,
//        configBuilder = commonBuilder(),
//        api = bggApi
//    )
//}

fun commonBuilder(): BallastViewModelConfiguration.Builder {
    return BallastViewModelConfiguration.Builder()
        .apply {
//            inputsDispatcher = Dispatchers.Main
//            eventsDispatcher = Dispatchers.Main
//            sideJobsDispatcher = Dispatchers.Default
//            interceptorDispatcher = Dispatchers.Default

            logger = ::AndroidBallastLogger

            this += LoggingInterceptor()
            this += BallastDebuggerInterceptor(debuggerConnection)
        }
}
