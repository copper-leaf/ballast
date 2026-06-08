# Ballast Scheduler Core

> [!CAUTION]
>
> Experimental. This module may not still have issues or changes in its public API before being considered stable.
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

Ballast Scheduler is a lightweight way to reliably run periodic work. This Core module is completely independent of 
Ballast's MVI system, and focuses on the specific problem of scheduling, and can be used without adopting the full MVI
architecture. 

This module provides several ways to run in-memory schedules (based on coroutines `delay()`, or with cron-like polling), 
as well as several basic schedules to run tasks on. Additional scheduling functionality is provided in other modules, 
linked in [See Also](#see-also) section below.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Scheduler Cron](./../ballast-scheduler-cron)
- [Ballast Scheduler ViewModel](./../ballast-scheduler-viewmodel)

## Usage

This library is based on the concept of a `Schedule`, which is a generator or `kotlin.time.Instant`s that a task should
be run on. A `Schedule` produces a `Sequence<Instant>` of future Instants from a given starting Instant, which declare
the ideal schedule for running tasks. A `ScheduleExecutor` is responsible for actually dispatching tasks to your 
application at the correct moment in time. Essentially, a ScheduleExecutor converts a `Sequence<Instant>` to 
`Flow<Instant>`, such that the collector of that Flow executes tasks at the proper time.

```kotlin
// Definition of a Schedule
fun interface Schedule {
    fun generateSchedule(start: Instant): Sequence<Instant>
}
```

A Schedule is required to always provide the _next_ moment in time after the start Instant. Some executors may 
instead execute the Schedule by only receiving the first element from `generateSchedule()`, then passing that value 
in to `generateSchedule()` again. This is not a direct collection of the Sequence but effectively produces the same 
result, and allows one to persist and resume the schedule state.

### ScheduleExecutors

#### Basic Usage

A `ScheduleExecutor` converts an ideal `Schedule` into a realtime `Flow` of tasks. Depending on how it's used, the 
resulting flow may apply backpressure to the upstream Schedule to deal withs scenarios where the task takes longer to 
run than the ideal delay between tasks. It is up to the implementation of the Executor whether backpressure can actually
be applies or not. 

All schedules have 2 modes of operation: 

`runSchedule(schedule: Schedule)`: This will run a single schedule, directly converting the schedule to a Flow. As a 
direct execution, it can potentially apply backpressure. 

```kotlin
val schedule = EveryMinuteSchedule()
val executor = DelayScheduleExecutor()

executor
    .runSchedule(schedule)
    .onEach {
        println("Executing scheduled task at ${it.triggeredAt}")
    }
    .launchIn(viewModelScope)
```

`runSchedules(schedules: List<NamedSchedule>)`: This will run multiple schedules in a back, emitting vales from each
of them to the same downstream `Flow` using [merge](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/merge.html).
Because the upstream Schedules are all merged concurrently, backpressure cannot be applied from the downstream Flow, 
as just one Schedule emitting too quickly would block the execution of other Schedules. Additionally, each Schedule 
must also be given a unique String name so emissions can be differentiated, using `Schedule.named(""")`.

```kotlin
val schedule1 = EveryMinuteSchedule().named("EveryMinuteSchedule")
val schedule2 = EverySecondSchedule().named("EverySecondSchedule")
val executor = DelayScheduleExecutor()

executor
    .runSchedules(listOf(schedule1, schedule2))
    .onEach {
        println("Executing scheduled task from ${it.name} at ${it.triggeredAt}")
    }
    .launchIn(viewModelScope)
```

#### DelayScheduleExecutor

The `DelayScheduleExecutor` runs tasks based on a simple Coroutine delay loop, where tasks are executed at the exact 
moment that the schedule requested (within a few milliseconds, typically). Collecting from single schedule with 
`runSchedule` will apply backpressure to the Schedule. If the collector is still collecting an element when the next
tick is triggered, that task will be dropped and a `onTaskDropped` lambda will be called with that instant for logging
or other recovery.

One method of applying backpressure is to add the `.adaptive()` operator on the upstream Schedule. This will take the
original Schedule's declared times and delay it by the actual time it took to process the task, effectively adapting the
schedule to consider the schedule as a declaration of delay between the end of one task and the start of another, rather
than the start of both tasks. This should prevent `onTaskDropped` from being called, as each emission would be 
guaranteed to be in the future.

The `DelayScheduleExecutor` is best suited for schedules of relatively short delays (on the order of minutes), where
exact timing guarantees are needed. It is also best when you need to run just one schedule with the ability to apply
backpressure or handle missed triggers.

#### PollingScheduleExecutor

For tasks that run less frequently, such as in server-side applications, backpressure is not as important as 
reliability. The `PollingScheduleExecutor` is inspired by a Cron-like processing loop, where only one delay loop is 
running, and each minute it checks for which Schedules would like to trigger a task during that minute. This processes
more efficiently, but at the cost of less precision, since tasks may be delayed by up to a minute from their ideal 
moment of execution.

It is not possible to apply backpressure to a Schedule with `PollingScheduleExecutor`, since internally it does not 
directly collect from the Schedule's sequence. Instead, the state of each schedule is stored in a 
`ScheduleExecutor.State`, where the previous execution of the schedule is used as the start for a new call to 
`generateSchedule()` every minute. You can use `InMemoryScheduleState` for storing these previous executions in memory, 
but it is advised to store this state in a persistent store in your application, such as a database table. 

**Configuration:**

`pollingSchedule` - You can poll at a different schedule besides every minute, to make the polling even more efficient. 
Any schedule you use to run tasks can also define the polling schedule. Note that if you run the schedule less 
frequently than once per minute, tasks may get skipped since it only checks for matching scheduled tasks in the 
_current_ minute, not since the last polled minute. Be sure to align your scheduled tasks to the polling schedule with 
`Schedule.alignTo()`

`catchUpBehavior` - If your application was not running when a scheduled task was supposed to run, it will be detected
the next time this executor starts processing. By default, a single task will be triggered to catch up, no matter how
many tasks were missed in the downtime. This can be configured to:

- `CatchUpBehavior.ExecuteOne` - process just the first task that was missed, and skip the ones after that.
- `CatchUpBehavior.ExecuteAll` - process all missed tasks one-by-one. It is up to you to either process those tasks 
  sequentially or in parallel and synchronizing between these tasks.
- `CatchUpBehavior.Skip` - Don't process any missed tasks. Just update the state and continue from the current moment in
  time.

### Schedules

There are a handful of basic schedules for basic tasks: `EveryDaySchedule`, `EveryHourSchedule`, `EveryMinuteSchedule`,
and `EverySecondSchedule`. By default, each of these execute at the "top" of the given timeframe (at midnight, at minute 
0 of the hour, etc.). You can instead provide a list of moments during the given timeframe (at midnight at noon, at minutes 
0, 15, 30, and 45 of each hour, etc.). 

Instead of triggering a schedule at an exact repeated moment, you can instead provide an arbitrary delay between tasks 
with `FixedDelaySchedule` or `ExponentialDelaySchedule`.

The last predefined schedule is `FixedInstantSchedule`, which allows you to provide an exact list of `Instants` to 
trigger your schedule. Note that unlike the other predefined schedules which are all _generators_ and provide an 
infinite sequence of tasks, this one has a fixed set of tasks to run, after which will it never trigger again.

### Schedule Operators

Schedules are fundamentally based on `Sequences`, so it's easy to customize the behavior of a predefined schedule. The
following operators are available out-of-the-box, but you're also welcome to build whatever other Sequence operators you
need to generate more custom scheduling behavior.

- `schedule.adaptive()`: mostly useful for the `FixedDelaySchedule`, to adjust the time between tasks by the amount of
  time it takes to process them.
- `schedule.alignTo(DurationUnit, TimeZone)`: Aligns each scheduled instant to the next boundary of the given
  time unit. For example, a schedule that fires at `:30` seconds past the minute, when aligned to
  `DurationUnit.MINUTES`, will instead fire at the top of the next minute (`:00`). Supported units are `SECONDS`,
  `MINUTES`, `HOURS`, and `DAYS`. This is particularly useful for aligning schedules to the
  `PollingScheduleExecutor`'s polling interval.
- `schedule.between(ClosedRange<Instant>)`: Filter emissions so that they are only handled during the given time range.
  Once the end of the range has been passed, the schedule will complete
- `schedule.startingAt(Instant)`: Delay the start of a schedule until a specified Instant
- `schedule.until(Instant)`: Process Inputs as long as they are before the end Instant. This makes the schedule finite;
  once the end time has been passed, the schedule will complete.
- `schedule.delayed(Duration)`: Delay the start of a schedule by a specified Duration 
- `schedule.delayedUntil(Instant)`: Delay the start of a schedule by a specified Duration 
- `schedule.filterByDayOfWeek(vararg dayOfWeek)`: Filters the scheduled instants so they only trigger on the specified
  days of the week. Related operators of `schedule.weekdays()` and `schedule.weekends()` are also available.
- `schedule.named(String)`: Provides a unique name to the Schedule so it can be batched with other schedules in the same 
  ScheduleExecutor.
- `schedule.take(Int)`: Only handle the first N emissions of the sequence. This makes the schedule finite, limited to at
  most N emissions.
- `schedule.getNext(Clock)`: Get the next trigger of the schedule after the current Instant
- `schedule.getNext(Instant)`: Get the next trigger of the schedule after a specified Instant
- `schedule.transform { squence -> sequence }`: Apply custom operators directly to the generated Sequence, returning a 
  new Schedule that encapsulates that transformation.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-scheduler-core:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-scheduler-core:{{ballastVersion}}")
            }
        }
    }
}
```
