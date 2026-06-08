# Ballast Scheduler Android AlarmManager

> [!CAUTION]
>
> Experimental. This module may not still have issues or changes in its public API before being considered stable.
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

A AlarmManager-based implementation of the Ballast Scheduler library for persistent, long-running scheduled tasks on
Android. Unlike in-memory schedulers which stop when the app is closed, this module uses AlarmManager to ensure 
scheduled tasks are reliably executed even when the app is not in the foreground. 

> [!NOTE]
> AlarmManager is intended for tasks where exact wall-clock timing is important, and such exact timing may have a 
> significant impact on device battery life if the alarms wake up the device frequently. Workmanager is more efficient 
> for batter life as it is inexact by nature and batches tasks together. But that efficiency has the tradeoff of really 
> only being useful for non user-visible tasks. 
> 
> WorkManager is great for non user-visible work, and this module is not intended to be a replacement for it. Rather, it
> serves the purpose of user-visible scheduling such as Calendar notifications, which WorkManager cannot reliably 
> handle.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ❌         |
| Android  | ✅         |
| iOS      | ❌         |
| JS       | ❌         |
| WASM JS  | ❌         |

## See Also

- [Ballast Scheduler Core](./../ballast-scheduler-core)
- [Ballast Scheduler Cron](./../ballast-scheduler-cron)
- [Ballast Scheduler ViewModel](./../ballast-scheduler-viewmodel)

## Usage

Initialize a `BallastAlarmManager` once (e.g. in your `Application.onCreate()`) by providing an
`EventDrivenScheduleExecutor` configured with an `AlarmManagerAdapter`. The executor handles registering, updating,
and cancelling alarms. A `BallastAlarmManagerBootCompletedWorker` receiver must also be registered in your
`AndroidManifest.xml` to re-sync scheduled alarms after device reboot.

```kotlin
// In Application.onCreate() or a DI module
val executor = BallastAlarmManager.initialize(
    executor = EventDrivenScheduleExecutor(
        adapter = AlarmManagerAdapter(applicationContext),
        state = SharedPreferencesScheduleState(applicationContext),
        scheduleSerializer = ExampleSchedule.serializer(),
        callbackSerializer = ExampleCallback.serializer(),
    ),
    precision = AlarmPrecision.Default, // setExact — change to High for setExactAndAllowWhileIdle
)

// Register a schedule
executor.registerOrUpdateSchedule(
    schedule = ExampleSchedule(name = "DailyReminder", cronExpression = "0 8 * * *"),
    callback = ExampleCallback(),
)
```

```xml
<!-- AndroidManifest.xml -->
<receiver android:name="com.copperleaf.ballast.scheduler.alarmmanager.BallastAlarmManagerScheduleWorker"
    android:exported="false" />
<receiver android:name="com.copperleaf.ballast.scheduler.alarmmanager.BallastAlarmManagerBootCompletedWorker"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-scheduler-android-alarmmanager:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-scheduler-android-alarmmanager:{{ballastVersion}}")
            }
        }
    }
}
```
