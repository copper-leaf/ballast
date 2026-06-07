# Ballast Scheduler ViewModel

> [!CAUTION]
>
> Experimental. This module may not still have issues or changes in its public API before being considered stable.
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

Integrates [Ballast Scheduler Core](./../ballast-scheduler-core) with the Ballast ViewModel system, allowing
schedules to dispatch Inputs directly to your ViewModels at the configured times with an in-memory non-persistent 
scheduler. Add the `SchedulerInterceptor` to your ViewModel configuration to attach one or more schedules to that 
ViewModel.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Scheduler Core](./../ballast-scheduler-core)
- [Ballast Scheduler Cron](./../ballast-scheduler-cron)

## Usage

Add a `SchedulerInterceptor` to your ViewModel configuration with a `SchedulerAdapter` that registers one or more
schedules. Each schedule produces a named `Instant` sequence from
[Ballast Scheduler Core](./../ballast-scheduler-core), and the interceptor dispatches the corresponding Input to
your ViewModel at each scheduled moment.

```kotlin
class ExampleViewModel(coroutineScope: CoroutineScope) : BasicViewModel<
        ExampleContract.Inputs,
        ExampleContract.Events,
        ExampleContract.State
        >(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            initialState = ExampleContract.State(),
            inputHandler = ExampleInputHandler(),
            name = "Example"
        )
        .apply {
            this += SchedulerInterceptor<
                    ExampleContract.Inputs,
                    ExampleContract.Events,
                    ExampleContract.State> {
                onSchedule(
                    schedule = EveryHourSchedule().named("HourlyRefresh"),
                    scheduledInput = { ExampleContract.Inputs.Refresh },
                )
                onSchedule(
                    schedule = EveryDaySchedule(LocalTime(2, 0)).named("DailyCleanup"),
                    scheduledInput = { ExampleContract.Inputs.Cleanup },
                )
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
    implementation("io.github.copper-leaf:ballast-scheduler-viewmodel:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-scheduler-viewmodel:{{ballastVersion}}")
            }
        }
    }
}
```
