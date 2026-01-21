package com.copperleaf.ballast.examples.presentation.queue

import com.copperleaf.ballast.autoscale.AutoscalingViewModel
import com.copperleaf.ballast.autoscale.ViewModelFactory
import com.copperleaf.ballast.autoscale.policies.FixedScalingPolicy
import com.copperleaf.ballast.examples.di.ComposeDesktopInjector
import com.copperleaf.ballast.examples.presentation.models.QueueName
import kotlinx.coroutines.CoroutineScope

class MainQueueViewModel(
    coroutineScope: CoroutineScope,
    injector: ComposeDesktopInjector,
) : AutoscalingViewModel<
        MainQueueContract.Inputs,
        MainQueueContract.Events,
        MainQueueContract.State>(
    coroutineScope = coroutineScope,
    factory = ViewModelFactory { coroutineScope: CoroutineScope, id: Int ->
        MainQueueViewModelWorker(coroutineScope, injector, QueueName.entries[id])
    },
    scalingPolicy = FixedScalingPolicy(QueueName.entries.size),
    distributionPolicy = MainQueueDistributionPolicy(),
)
