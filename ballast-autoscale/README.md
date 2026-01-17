# Ballast Autoscale

> [!CAUTION]
> 
> Experimental. This module may not still have issues or changes in its public API before being considered stable. 
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

`AutoscalingViewModel` acts as a wrapper around a pool of other ViewModels, and provides basic facilities for scaling 
the pool of ViewModels up or down to adapt to load, and distributing work among the pool of ViewModel workers. The main
use-case would be in server-side applications such as job queue processors. For example, one could increase the 
parallelism of processing jobs in the queue in response to the number of pending jobs, average time spent waiting for a 
job to start, etc.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Ktor Server](./../ballast-ktor-server/README.md)
- [Ballast Queue Core](./../ballast-queue-core/README.md)
- [Ballast Scheduler ViewModel](./../ballast-scheduler-viewmodel/README.md)

## Usage

This module introduces a new implementation of `BallastViewModel`: `AutoscalingViewModel`. This new ViewModel type acts
as a wrapper around a pool of ViewModels of the same type, automatically adding or removing instances as needed to 
respond to system pressure. It's intended to be used in a server-side context, most specifically in conjunction with
[Ballast Queue](./../ballast-queue-core/README.md), though it intentionally does not depend on any functionality that 
would prevent it from being used in frontend apps or anywhere else.

Your application code should treat the `AutoscalingViewModel` exactly the same as it if were a `BasicViewModel`, sending
Inputs to it as normal. It will then distribute those Inputs to one of the inner ViewModels to be enqueued and handled 
as normal. There are 3 components that need to be provided to the `AutoscalingViewModel` to allow this autoscaling 
functionality to work:

### ViewModelFactory

A `ViewModelFactory` is responsible for creating a new copy of a ViewModel so that it can be run within the "cluster". 
The factory is provided a CoroutineScope which is a child of the scope passed to `AutoscalingViewModel`, and an integer 
ID which should be used to give the VM a unique name. 

The ID provided to the factory function is the numerical index indicating its position in the current pool. IDs may be 
reused if the cluster scales down, then back up, so it's not globally unique. However, it is intended to be stable such
that it can be used as a property to determine how configure the ViewModel. For example, you may want to attach a
`SchedulingInterceptor` from [Ballast Scheduler ViewModel](./../ballast-scheduler-viewmodel/README.md) to enqueue 
maintenance tasks on a schedule, but you only want 1 replica to enqueue those tasks so you don't have to manually
deduplicate those jobs. For this case, you could configure the ViewModel to only attach the SchedulerInterceptor at 
`ID: 0`.

The ViewModels produced by this factory should run on the provided CoroutineScope, and will get closed automatically if 
the `AutoscalingViewModel` gets closed. When a ViewModel gets removed from the cluster, it will be shut down gracefully
to try and allow in-progress Inputs to complete, using `BallastViewModel.close()`.

### ScalingPolicy

The `ScalingPolicy` returns a `Flow<Int>` which indicates how many replicas of the inner ViewModel you need running. The 
Flow must always request at least 1 replica. `FixedScalingPolicy` is the only implementation provided by default, which 
allows you to set a fixed number of replicas which all get created immediately and never get scaled down. 

Your application may instead need adjust the number of ViewModels in the pool dynamically based on real measured 
pressure from your system. It is up to you to determine how to measure this pressure and determine how many replicas you 
need. 

### DistributionPolicy

Once the cluster is up and running and ready to accept Inputs, the `AutoscalingViewModel` will distribute the Inputs 
its receives to exactly one of the ViewModels running in the cluster. The `DistributionPolicy` is responsible for 
selecting a viewModel in the pool and allowing the `AutoscalingViewModel` to forward the Input to it.

Several Distribution Policies are provided by default:

- `LeaderDistributionPolicy`: the first ViewModel in the pool will receive all Inputs, which ensures all Inputs are 
  processed sequentially. This can be used to have the Leader insert the Input into a shared queue to be processed 
  later, such as a database table or SQS queue.
- `RoundRobinDistributionPolicy`: ViewModels are selected in a round-robin fashion, so no ViewModel will receive two 
  Inputs in a row (unless there's only 1 in the pool, of course). This may a good choice when using 
  `FixedScalingPolicy`, which will ensure all ViewModels in the pool receive an equal number of Inputs. These Inputs 
  will then be processed in parallel by all ViewModels in the cluster.
- `RandomDistributionPolicy`: ViewModels are selected randomly. This may cause some ViewModels to receive more 
  Inputs than others, but will help with distributing the load when ViewModels are scaling up and down quickly as Round
  Robin would tend to favor ViewModels with lower IDs, as the Round Robin index may wrap around and skip a newly-added
  ViewModel. These Inputs will then be processed in parallel by all ViewModels in the cluster.

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-autoscale:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-autoscale:{{ballastVersion}}")
            }
        }
    }
}
```
