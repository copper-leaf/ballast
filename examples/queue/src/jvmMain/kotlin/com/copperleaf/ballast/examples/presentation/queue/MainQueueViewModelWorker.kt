package com.copperleaf.ballast.examples.presentation.queue

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.core.PrintlnLogger
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.examples.di.ComposeDesktopInjector
import com.copperleaf.ballast.examples.presentation.models.QueueName
import com.copperleaf.ballast.queue.JobQueueInputStrategy
import com.copperleaf.ballast.withSerialization
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MainQueueViewModelWorker(
    coroutineScope: CoroutineScope,
    injector: ComposeDesktopInjector,
    queue: QueueName,
) : BasicViewModel<
        MainQueueContract.Inputs,
        MainQueueContract.Events,
        MainQueueContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            initialState = MainQueueContract.State(),
            inputHandler = MainQueueInputHandler(),
            name = "MainQueueViewModelWorker-${queue.name}",
        )
        .withSerialization(
            inputsSerializer = MainQueueContract.Inputs.serializer(),
            eventsSerializer = MainQueueContract.Events.serializer(),
            stateSerializer = MainQueueContract.State.serializer(),
        )
        .apply {
            inputStrategy = JobQueueInputStrategy(
                queueName = queue.name,
                driver = injector.driver,
                adapter = MainQueueAdapter(),
                captureErrorStacktrace = true,
            )

            interceptors += LoggingInterceptor()
            logger = ::PrintlnLogger
        }
        .build(),
    eventHandler = eventHandler { },
)
