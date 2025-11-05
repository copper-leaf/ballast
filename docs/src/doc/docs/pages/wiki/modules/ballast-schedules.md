---
---

## Overview

Ballast Scheduler is still a work in progress. Any features/APIs described here might change at any time.

Ballast Scheduler is a simple way to run periodic work, similar to [Spring @Scheduled][1] or the [Java Timer][2], by 
dispatching an Input to one of your ViewModels on a configurable schedule. It supports both non-persistent work on all 
platforms by being embedded into an existing ViewModel and running purely on coroutines, and also experimental support 
for persistent work by running on [Android WorkManager][3].

## Basic Usage

### Schedule Adapter

To start, we need to define our scheduled work, which is done by creating an instance of `ScheduleAdapter`. Within the
adapter, we can set up one or more schedules to generate a sequence of Instants which should handle a specific type of
Input.

A basic adapter looks like this:

```kotlin
public class BallastSchedulerExampleAdapter : SchedulerAdapter<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State> {
    override suspend fun SchedulerAdapterScope<
            ExampleContract.Inputs,
            ExampleContract.Events,
            ExampleContract.State>.configureSchedules() {
        onSchedule(
            key = "Every 30 Minutes",
            schedule = EveryHourSchedule(0, 30),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(1) },
        )
        onSchedule(
            key = "Daily at 2am",
            schedule = EveryDaySchedule(LocalTime(2, 0)),
            scheduledInput = { ExampleContract.Inputs.Increment(1) },
        )
    }
}
```

### Embedded Scheduler

An Embedded Scheduler is installed into an existing Ballast ViewModel as an Interceptor. By sending an instance of
`SchedulerAdapter` to the Interceptor, you can start register a scheduled task. `SchedulerAdapter` is a `fun interface`,
so it can be passed to the `SchedulerInterceptor` as a lambda, and within the lambda you may register multiple
Schedules. 

```kotlin
val vm = BasicViewModel(
    coroutineScope = viewModelCoroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example"
        )
        .apply {
            // pass an Adapter class instance
            this += SchedulerInterceptor(BallastSchedulerExampleAdapter())
            
            // or set up the schedules as a lambda
            this += SchedulerInterceptor {
                onSchedule(
                    key = "Every 30 Minutes",
                    schedule = EveryHourSchedule(0, 30),
                    scheduledInput = { SchedulerExampleContract.Inputs.Increment(1) },
                )
                onSchedule(
                    key = "Daily at 2am",
                    schedule = EveryDaySchedule(LocalTime(2, 0)),
                    scheduledInput = { ExampleContract.Inputs.Increment(1) },
                )
            }
        }
        .build(),
    eventHandler = ExampleEventHandler(),
)
```

Schedules can also be created dynamically from within the attached ViewModel's InputHandler:

```kotlin
class ExampleInputHandler : InputHandler<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State> {
    override suspend fun InputHandlerScope<
            ExampleContract.Inputs,
            ExampleContract.Events,
            ExampleContract.State>.handleInput(
        input: ExampleContract.Inputs
    ) = when (input) {
        is ExampleContract.Inputs.StartSchedules -> {
            sideJob("Start schedules") {
                scheduler().send(
                    SchedulerContract.Inputs.StartSchedules {
                        onSchedule(
                            key = "Daily at 2am",
                            schedule = EveryDaySchedule(LocalTime(2, 0)),
                        ) {
                            ExampleContract.Inputs.Increment(1)
                        }
                    }
                )
            }
        }
    }
}
```

The Scheduler is embedded into another ViewModel and sends Inputs back to it on the defined schedules, but it is itself
also a ViewModel! This means you can add other Interceptors like Logging and Debugging into the Scheduler to observe or
augment its functionality. The Configuration must include `.withSchedulerController()`.

```kotlin
this += SchedulerInterceptor(
    config = BallastViewModelConfiguration.Builder()
        .withSchedulerController<
                ExampleContract.Inputs,
                ExampleContract.Events,
                ExampleContract.State>()
        .apply { 
            this += LoggingInterceptor()
            logger = ::PrintlnLogger
        }
        .build(),
    initialSchedule = {
        onSchedule(
            key = "Daily at 2am",
            schedule = EveryDaySchedule(LocalTime(2, 0)),
        ) {
            ExampleContract.Inputs.Increment("", 1)
        }
    }
)
```

### Android WorkManager

Ballast Scheduler also supports persistent work on Android by configuring a schedule to run on top of WorkManager, 
instead of embedded within a ViewModel. The general process is the same, but there are some restrictions to be aware of.
Most notably, you cannot use a lambda to create your `SchedulerAdapter`, since WorkManager needs to persist the state of
the schedule and rehydrate it later when each scheduled task is handled. It does this by using reflection to create your
`SchedulerAdapter` class, then determining the next Instant to run a Unique `OneTimeWorkRequest`. The Inputs generated
on each schedule "tick" are also passed back to a `SchedulerCallback` class (only available on Android targets), since 
it is not directly connected to a ViewModel. You should forward that Input to a ViewModel so it is processed by Ballast 
as normal.

It is advised to use the [Android Startup library][5] to initialize your schedules, and to not create them dynamically 
like you can with an embedded scheduler. Ballast Scheduler needs to be able to regularly sync its own schedule state and 
configuration with WorkManager. Schedules can be synced anytime the app starts up with 
`WorkManager.syncSchedulesOnStartup`, or synced periodically without needing to open the app with 
`WorkManager.syncSchedulesPeriodically`.

Running Ballast Schedules on WorkManager does not support setting constraints. You will need to check at runtime when 
handling the Input any constraints you wish to apply.

```kotlin
public class BallastSchedulerExampleAdapter : SchedulerAdapter<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State>, Function1<ExampleContract.Inputs, Unit> {
    override suspend fun SchedulerAdapterScope<
            ExampleContract.Inputs,
            ExampleContract.Events,
            ExampleContract.State>.configureSchedules() {
        onSchedule(
            key = "Every 30 Minutes",
            schedule = EveryHourSchedule(0, 30),
            scheduledInput = { SchedulerExampleContract.Inputs.Increment(1) }
        )
    }

    override fun invoke(p1: ExampleContract.Inputs) {
        AppInjector.get().exampleViewModel().trySend(p1)
    }
}
```

```kotlin
internal class BallastSchedulerExampleCallback : SchedulerCallback<BallastSchedulerExampleContract.Inputs>, KoinComponent {
    val vm: BallastSchedulerExampleViewModel by inject()

    override suspend fun dispatchInput(input: BallastSchedulerExampleContract.Inputs) {
        vm.sendAndAwaitCompletion(input)
    }
}
```

```kotlin
public class BallastSchedulerStartup : Initializer<Unit> {
  override fun create(context: Context) {
    val workManager = WorkManager.getInstance(context)

    workManager.syncSchedulesOnStartup(
      adapter = BallastSchedulerExampleAdapter(), 
      callback = BallastSchedulerExampleCallback(),
      withHistory = false
    )
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return listOf(WorkManagerInitializer::class.java)
  }
}
```

!!! warning

    Since WorkManager schedules are started via reflection, they might get removed by R8 as they are not referenced 
    directly in your code. Make sure to add `-keep` declarations to your `proguard-rules.pro` file to ensure these classes
    are not accidentally removed by R8 during minification.
    
    ```
    -keep class com.example.BallastSchedulerExampleAdapter
    -keep class com.example.BallastSchedulerExampleCallback
    ```

### iOS BGTaskScheduler

Running persistent scheduled work on iOS is not yet implemented. Ideally, it would work very similarly to running on 
WorkManager, but using something like iOS's [BGTaskScheduler][6]

## Schedule Configuration

A `Schedule` produces a Sequence of the kotlin-datetime `Instant` (`Sequence<Instant>`) given a starting `Instant`. It 
is generally considered to be an _ideal version_ of the schedule, but depending on how long it takes to process the 
Inputs dispatched by the schedule, the actual time that an Input is sent may be later, or some of the scheduled events 
may be dropped. 

Several schedule types are available, but you are free to implement the `Schedule` interface yourself and provide a 
custom sequence of scheduled tasks. 

### Delay Mode

When configuring a Schedule, you may choose whether you want the Inputs to be "fire-and-forget" type tasks, or
whether the schedule executor should suspend until one scheduled Input is completely processed before attempting to run
the next scheduled task. `ScheduleExecutor.DelayMode.FireAndForget` is the default.

```kotlin
public class BallastSchedulerExampleAdapter : SchedulerAdapter<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State> {
    override suspend fun SchedulerAdapterScope<
            ExampleContract.Inputs,
            ExampleContract.Events,
            ExampleContract.State>.configureSchedules() {
        onSchedule(
            key = "Daily at 2am",
            delayMode = ScheduleExecutor.DelayMode.Suspend,
            schedule = EveryDaySchedule(LocalTime(2, 0)),
        ) {
            ExampleContract.Inputs.Increment(1)
        }
    }
}
```

`ScheduleExecutor.DelayMode.FireAndForget` will dispatch the Inputs as closely to the ideal schedule as possible, but
may end up posting one Input before the previous one has completed, at which point the host ViewModel's InputStrategy
will determine how they two events are handled, as normal. `ScheduleExecutor.DelayMode.Suspend` will suspend the
execution of the schedule while one Input is still processing, potentially dropping scheduled tasks to ensure that one
Input finishes processing before sending the next one.

### Fixed Delay Schedule

The most basic type of `Schedule` is `FixedDelaySchedule`. It simply delays each subsequent task by a fixed `Duration` 
from the starting `Instant`. For example, a `FixedDelaySchedule(10.minutes)` starting at 6:04pm will send Inputs at 
6:14pm, 6:24pm, 6:34pm, etc. It has a strict minimum resolution of 1ms.

Alternatively, you may wish that a minimum amount of time is delayed between the end of one Input's processing, and the 
start of the next Input. In this case, use `FixedDelaySchedule(10.minutes).adaptive()` with the 
`ScheduleExecutor.DelayMode.Suspend` delay mode to adjust the schedule to account for processing time.

### Time-Based

There are also schedules which send Inputs at specific times of the day.  

`EveryDaySchedule` lets you send Inputs at a specific `LocalTime`. Multiple times may be configured to send Inputs 
multiple times each day.

`EveryHourSchedule` lets you send Inputs at a specific minute of the hour (at 0 seconds). Multiple minutes may be 
configured to send Inputs multiple times each hour.

`EveryMinuteSchedule` lets you send Inputs at a specific second of the minute (at 0 ms). Multiple seconds may be
configured to send Inputs multiple times each minute.

`EverySecondSchedule` lets you send Inputs once every second, precisely at the start of the second. Useful for things 
like showing countdown timers in the UI that need to be synchronized to the wall clock, in contrast to using 
`FixedDelaySchedule(1.seconds)` which will drift over time. 

### Fixed Instant Schedule

For cases where your application logic has already computed the Instants to trigger the schedule, `FixedInstantSchedule` 
will send those exact Instants according to the system `Clock`. At each iteration of this schedule, the next Instant
after the current Clock time will be sent, and the entire schedule will be completed once the System clock has advanced
past all provided Instants.

### (TODO) Cron Expression

Cron expressions are not yet supported.

### Schedule Operators

Schedules are fundamentally based on `Sequences`, so it's easy to customize the behavior of a predefined schedule. The 
following operators are available out-of-the-box, but you're also welcome to use whatever other Sequence operators you 
need to generate more custom scheduling behavior.

- `schedule.adaptive()`: mostly useful for the `FixedDelaySchedule`, to adjust the time between tasks by the amount of
  time it takes to process them.
- `schedule.delayed(Duration)`: Delay the start of a schedule by a specified Duration
- `schedule.delayedUntil(Instant)`: Delay the start of a schedule until a specified Instant
- `schedule.bounded(ClosedRange<Instant>)`: Filter emissions so that they are only handled during the given time range. 
  Once the end of the range has been passed, the schedule will complete 
- `schedule.until(Instant)`: Process Inputs as long as they are before the end Instant. This makes the schedule finite; 
  once the end time has been passed, the schedule will complete.
- `schedule.filterByDayOfWeek(vararg dayOfWeek)`: Filters the scheduled instants so they only trigger on the specified
  days of the week. Related operators of `schedule.weekdays()` and `schedule.weekends()` are also available.
- `schedule.take(Int)`: Only handle the first N emissions of the sequence. This makes the schedule finite, limited to at
  most N emissions.
- `schedule.transform { squence -> sequence }`: Apply custom operators directly to the generated Sequence.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-schedules:{{gradle.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-schedules:{{gradle.version}}")
            }
        }
    }
}
```

[1]: https://www.baeldung.com/spring-scheduled-tasks
[2]: https://docs.oracle.com/javase/8/docs/api/java/util/Timer.html
[3]: https://developer.android.com/topic/libraries/architecture/workmanager
[4]: https://github.com/Kotlin/kotlinx-datetime
[5]: https://developer.android.com/topic/libraries/app-startup
[6]: https://developer.apple.com/documentation/backgroundtasks
