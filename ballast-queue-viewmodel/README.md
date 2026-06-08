# Ballast Queue ViewModel

> [!CAUTION]
>
> Experimental. This module may not still have issues or changes in its public API before being considered stable.
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

Use the familiar Ballast ViewModel structure as the interface to a persistent job queue, allowing similar code patterns 
and semantics for both client-side and server-side workloads.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Queue Core](./../ballast-queue-core)
- [Ballast Kotlinx Serialization](./../ballast-kotlinx-serialization)
- [Ballast Ktor Server](./../ballast-ktor-server)

## Usage

This module wraps a `QueueDriver` from [Ballast Queue Core](./../ballast-queue-core) and exposes its functionality
through a Ballast ViewModel. This allows you to use all of the classes you're already familiar with for UI ViewModels
and apply it to persistent queues. 

A Job Queue is created as described in [Ballast Queue Core](./../ballast-queue-core/README.md#setting-up-a-queue). 
You should familiarize yourself with the concepts of the base Queue module, as all that functionality will be supported 
here, but will be exposed through the Ballast ViewModel API. 

To use Ballast ViewModels as a queue executor, you will create a ViewModel and set its InputStrategy to 
`JobQueueInputStrategy`, which internally creates and interacts with the executor. Internally, it will create the
`DefaultQueueExecutor` and submit and pull jobs from that executor.

From there, you can use all the features of normal ViewModels, such as sending Inputs, processing them with an 
InputHandler, updating state, and using SideJobs. There are some notable differences to some features of the Viewmodel,
though:

- ViewModels using `JobQueueInputStrategy` do not contain a `StateFlow` and have no external state to observe. State is
  maintained individually for each job in the queue, rather than globally in the viewModel, so calls to 
  `getCurrentState()`, `updateState { }`, etc. are delegated to [the driver's job state](./../ballast-queue-core/README.md#step-5-processing-the-job-with-state).
- The semantics of `Events` is different. Rather than using Events as a way to communicate with the UI, the `JobQueueInputStrategy`
  uses an Event as the way to provide a [success result](./../ballast-queue-core/README.md#step-6-job-results), since 
  InputHandlers only return `Unit` and cannot return a value. Only one Event may be posted during the processing of a 
  job; attempts to post multiple events with throw an exception and fail the job. Events posted from SideJobs or 
  Interceptors will similarly fail. Events may be posted anywhere in the InputHandler during the processing of a job, 
  but the result will only be stored if the job completes successfully. Additionally, these Events are _not_ sent to an
  `EventHandler`, which is not used by ViewModels using `JobQueueInputStrategy`.
- Some Interceptors may not work correctly, since the semantics of state updates and events is different from a 
  traditional UI ViewModel. The `JobQueueInputStrategy` does send Notifications whenever relevant for the purposes of 
  logging an observability, but features like [Sync](./../ballast-sync), [Saved State](./../ballast-sync), etc. will not
  work correctly since they depend on a specific ordering of events relating to States and Events.
- [Testing](./../ballast-test) should work correctly, but make sure to use the `SyncQueueDriver`.
- SideJobs work as normal, and are the intended way to chain multiple jobs together in a pipeline by using `postInput()`
  from a SideJob. You can even observe flows in a sideJob to enqueue jobs regularly, but the 
  [Ballast Scheduler](./../ballast-scheduler-viewmodel) is recommended for greater control and safety around running 
  regularly-scheduled tasks. SideJobs are only dispatched if the inputHandler function returns successfully, indicating 
  job success.

### Complete Example

This example shows how one can set up a ViewModel as the Queue interface, with multiple independent workers, 
observability via logging, automatic serialization/deserialization, repeating jobs, and durable database storage. 

Uses the following Ballast modules:

- [Ballast Core](./../ballast-core)
- [Ballast Queue Core](./../ballast-queue-core)
- [Ballast Queue Exposed Driver](./../ballast-queue-exposed-driver)
- [Ballast Kotlinx Serialization](./../ballast-kotlinx-serialization)
- [Ballast Scheduler Core](./../ballast-scheduler-core)
- [Ballast Scheduler ViewModel](./../ballast-scheduler-viewmodel)
- [Ballast Scheduler Cron](./../ballast-scheduler-cron)
- [Ballast Autoscale](./../ballast-autoscale)

```kotlin

// Create an AutoscalingViewModel to run 4 copies of your queue in parallel. Store this ViewModel as a singleton and 
// send jobs to the queue with JobMaintenanceViewModel.send(), which get distributed to a worker and persisted in the 
// database queue.
class JobQueueViewModel(
    coroutineScope: CoroutineScope,
) : AutoscalingViewModel<
        JobQueueContract.Inputs,
        JobQueueContract.Events,
        JobQueueContract.State>(
    coroutineScope = coroutineScope,
    factory = ViewModelFactory { workerScope, id ->
        koin.get<JobQueueViewModelWorker> { params(workerScope, id) }
    },
    scalingPolicy = FixedScalingPolicy(4),
    distributionPolicy = RoundRobinDistributionPolicy(),
)

// the Worker uses JobQueueInputStrategy to enable persistent 
// queues, and `SchedulerInterceptor` to enqueue a task on a
// regular cadence.
private class JobQueueViewModelWorker(
    private val coroutineScope: CoroutineScope,
    private val id: Int,
    private val inputHandler: JobQueueInputHandler,
    private val repository: JobsRepository,
) : BasicViewModel<
        JobQueueContract.Inputs,
        JobQueueContract.Events,
        JobQueueContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            inputHandler = inputHandler,
            initialState = JobQueueContract.State,
            name = "JobQueueViewModel-$id"
        )
        .withSerialization(
            inputsSerializer = JobQueueContract.Inputs.serializer(),
            eventsSerializer = JobQueueContract.Events.serializer(),
            stateSerializer = JobQueueContract.State.serializer(),
        )
        .apply {
            logger = ::PrintlnLogger

            inputStrategy = JobQueueInputStrategy(
                queueName = "default",
                driver = ExposedDatabaseQueueDriver(repository),
                adapter = ExposedDatabaseQueueDriver.DefaultAdapter(),
            )

            interceptors += LoggingInterceptor()
            interceptors += SchedulerInterceptor {
                onSchedule(
                    schedule = CronSchedule(CronExpression.parse("0 * * * *")).named("every hour"),
                ) { JobQueueContract.Inputs.RepeatedJob } 
            }
        }
        .build(),
    eventHandler = eventHandler { },
)
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-queue-viewmodel:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-queue-viewmodel:{{ballastVersion}}")
            }
        }
    }
}
```
