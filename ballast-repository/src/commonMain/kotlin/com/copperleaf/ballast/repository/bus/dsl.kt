package com.copperleaf.ballast.repository.bus

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Collect Inputs of the type allowed by this ViewModel that were emitted to the EventBus.
 *
 * TODO: update to use Kotlin Context Receivers (https://blog.jetbrains.com/kotlin/2022/02/kotlin-1-6-20-m1-released/#prototype-of-context-receivers-for-kotlin-jvm)
 *  to only allow this to be called from an InputHandler
 */
public inline fun <reified T> EventBus.observeInputsFromBus(): Flow<T> = events
    .filterIsInstance<T>()
