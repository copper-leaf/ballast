package com.copperleaf.ballast.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

public interface RestartableJob<T> : Job {
    public fun restart(context: T)
}

public fun <T> CoroutineScope.restartableJob(
    block: suspend CoroutineScope.(T) -> Unit,
): RestartableJob<T> {
    val channel = Channel<T>(Channel.CONFLATED)
    val job = launch {
        channel.consumeAsFlow().collectLatest { block(it) }
    }
    return object : RestartableJob<T>, Job by job {
        override fun restart(context: T) {
            channel.trySend(context)
        }
    }
}
