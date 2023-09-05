package com.copperleaf.ballast.contracts.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

public class CoroutineScopeInfo(
    val dispatcher: CoroutineDispatcher?,
    val uncaughtExceptionHandler: CoroutineExceptionHandler?,
    val fullContext: CoroutineContext,
)

@OptIn(ExperimentalStdlibApi::class)
suspend fun getCoroutineScopeInfo(): CoroutineScopeInfo {
    return CoroutineScopeInfo(
        dispatcher = coroutineContext[CoroutineDispatcher],
        uncaughtExceptionHandler = coroutineContext[CoroutineExceptionHandler],
        fullContext = coroutineContext,
    )
}
