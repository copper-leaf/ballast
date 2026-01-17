# Ballast Scheduler Cron

> [!CAUTION]
>
> Experimental. This module may not still have issues or changes in its public API before being considered stable.
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Scheduler Core](./../ballast-scheduler-core/README.md)
- [Ballast Scheduler ViewModel](./../ballast-scheduler-viewmodel/README.md)

## Usage

This module adds a `CronSchedule` implementation of `Schedule` for scheduling tasks using the familiar Unix-style Cron 
syntax. [crontab.guru](https://crontab.guru/) is a helpful resource for interpreting Cron expressions, which supports 
the same 5-field syntax as Ballast.

A basic Cron schedule can be created with `CronExpression.parse(expression: String)`. 

```kotlin
CronExpression.parse("0 0 * * SUN")
```

Alternatively, you can create the fields directly using the structured constructor.

```kotlin
CronExpression(
    minute = MinuteField.exactValue(0),
    hour = HourField.exactValue(0),
    dayOfMonth = DayOfMonthField.anyValue(),
    month = MonthField.anyValue(),
    dayOfWeek = DayOfWeekField.exactValue(DayOfWeek.SUNDAY),
)
```

### Cron Syntax

This Cron implementation abides by the syntax and semantics defined by the [Open Cron Pattern Specification](https://github.com/open-source-cron/ocps).

It currently supports [Version 1.0](https://github.com/open-source-cron/ocps/blob/main/specifications/OCPS-1.0.md)
of the specification. Here's a summary of the OCPS syntax supported by Ballast:

**Field Values**

`MINUTE HOUR DAY-OF-MONTH MONTH DAY-OF-WEEK`

| Field            | Required | Allowed Values  |
|:-----------------|:---------|:----------------|
| **Minute**       | Yes      | 0-59            |
| **Hour**         | Yes      | 0-23            |
| **Day of Month** | Yes      | 1-31            |
| **Month**        | Yes      | 1-12 or JAN-DEC |
| **Day of Week**  | Yes      | 0-7 or SUN-SAT  |

* Month and Day of Week names are case-insensitive.
* In the Day of Week field, `0` and `7` are both treated as Sunday.

**Month Name Equivalents**

| Name | Numeric Value |
|:-----|:--------------|
| JAN  | 1             |
| FEB  | 2             |
| MAR  | 3             |
| APR  | 4             |
| MAY  | 5             |
| JUN  | 6             |
| JUL  | 7             |
| AUG  | 8             |
| SEP  | 9             |
| OCT  | 10            |
| NOV  | 11            |
| DEC  | 12            |

**Day of Week Name Equivalents**

| Name | Numeric Value |
|:-----|:--------------|
| SUN  | 0 or 7        |
| MON  | 1             |
| TUE  | 2             |
| WED  | 3             |
| THU  | 4             |
| FRI  | 5             |
| SAT  | 6             |

**Special Characters**

| Character | Name           | Example      | Description                                                                                                |
|:----------|:---------------|:-------------|:-----------------------------------------------------------------------------------------------------------|
| `*`       | Wildcard       | `* * * * *`  | Matches every allowed value for the field.                                                                 |
| `,`       | List Separator | `0,15,30,45` | Specifies a list of individual values.                                                                     |
| `-`       | Range          | `9-17`       | Specifies an inclusive range of values.                                                                    |
| `/`       | Step           | `5-59/15`    | Specifies an interval. The step operates on the range it modifies, yielding `5,20,35,50` for this example. |

Refer to the table below for the roadmap for supporting other versions of the OCPS specification:

| Version                                                                                    | Main Feature                                              | Supported in Ballast Version | Support Planned?            |
|--------------------------------------------------------------------------------------------|-----------------------------------------------------------|------------------------------|-----------------------------|
| [1.0](https://github.com/open-source-cron/ocps/blob/main/specifications/OCPS-1.0.md)       | 5-field syntax with minute precision                      | 5.1.0                        |                             |
| [1.1](https://github.com/open-source-cron/ocps/blob/main/increments/OCPS-increment-1.1.md) | Nicknames as aliases for common expressions               | Not Currently Supported      | Yes                         |
| [1.2](https://github.com/open-source-cron/ocps/blob/main/increments/OCPS-increment-1.2.md) | 6- and 7-field syntax for Second and Year-Level Precision | Not Currently Supported      | Yes                         |
| [1.3](https://github.com/open-source-cron/ocps/blob/main/increments/OCPS-increment-1.3.md) | Quartz-style field modifiers (`L`, `#`, `W`)              | Not Currently Supported      | With community contribution |
| [1.4](https://github.com/open-source-cron/ocps/blob/main/increments/OCPS-increment-1.4.md) | Logical operators                                         | Not Currently Supported      | With community contribution |

### Timezones

The OCPS specifies "A compliant parser or scheduler MUST interpret the pattern against the implementation's local time."
In layman's terms, this means that a Cron expression evaluates schedules against a local wall-clock, not against 
specific moments in time. In practical implementation terms, this means that the expression is always evaluated against a 
specific TimeZone, which must be provided at the time of creation.

```kotlin
// this expression will trigger at 06:00:00 in UTC
CronExpression.parse("0 0 * * SUN", timezone = TimeZone.of("America/Chicago"))

// this expression will trigger at 00:00:00 in UTC
CronExpression.parse("0 0 * * SUN", timezone = TimeZone.UTC)
```

The default timezone is `UTC`, which is the safest server-side default as it will not experience any Daylight Savings
transitions, leading to the most reliable and least surprising scheduling.

However, it may be useful to provide other timezones for end-user facing scenarios, such as sending a Weekly Summary 
email to users at 8am on Sundays at their own local time. Using other timezones will correctly handle things like 
Daylight Savings transitions.

### Example usage

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
            this += SchedulerInterceptor {
                onSchedule(
                    schedule = CronExpression.parse("0 0 * * SUN").named("Sunday at midnight"),
                    scheduledInput = { ExampleContract.Inputs.PerformDatabaseMaintenance },
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
    implementation("io.github.copper-leaf:ballast-scheduler-cron:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-scheduler-cron:{{ballastVersion}}")
            }
        }
    }
}
```
