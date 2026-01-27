# Ballast Ktor Server

> [!CAUTION]
>
> Experimental. This module may not still have issues or changes in its public API before being considered stable.
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

A Ktor plugin to integrate Ballast ViewModels into server-side Ktor services. Intended to be used with other server-side
Ballast components like Schedulers, Job Queues, and autoscaling.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ❌         |
| iOS      | ❌         |
| JS       | ❌         |
| WASM JS  | ❌         |

## See Also

- [Ballast Autoscale](./../ballast-autoscale)
- [Ballast Scheduler ViewModel](./../ballast-scheduler-viewmodel)
- [Ballast Queue ViewModel](./../ballast-queue-viewmodel)

## Usage

This module provides basic functionality for registering ViewModels to be used in your Ktor server, which start up and
get shut down with the application server's lifecycle. 

ViewModels must be registered using an `AttributeKey` so it can be accessed from an `ApplicationCall` with 
`ballastViewModel(key)`. This allows you to obtain a reference to the singleton ViewModel so you can send Inputs to it
from Request handlers.

```kotlin
class EmailQueueViewModel(
    private val coroutineScope: CoroutineScope,
) : BasicViewModel<
        EmailQueueContract.Inputs,
        EmailQueueContract.Events,
        EmailQueueContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .withViewModel(
            inputHandler = EmailQueueInputHandler(),
            initialState = EmailQueueContract.State,
            name = EmailQueueViewModel.Key.name,
        )
        .build(),
    eventHandler = eventHandler { },
) { 
    companion object {
        val Key = AttributeKey<EmailQueueViewModel>("EmailQueueViewModel")
    }
}

fun Application.module() {
    install(Ballast) {
        viewModel(
            attributeKey = EmailQueueViewModel.Key,
            createViewModel = { coroutineScope ->
                EmailQueueViewModel(coroutineScope)
            }
        )
    }

    routing {
        post("/send-email") {
            // dispatch a Ballast Input to send an email in the background. Suspends until the Input has been enqueued,
            // but does not wait for processing
            ballastViewModel(EmailQueueViewModel.Key).send(EmailQueueContract.Inputs.SendEmail())
            
            // return a response quickly so the application stays responsive for the end-user. The Input will be 
            // processed in the background to achieve eventual consistency
            call.respondText("Hello")
        }
    }
}
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM projects
dependencies {
    implementation("io.github.copper-leaf:ballast-ktor-server:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-ktor-server:{{ballastVersion}}")
            }
        }
    }
}
```
