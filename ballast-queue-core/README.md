# Ballast Queue Core

> [!CAUTION]
>
> Experimental. This module may not still have issues or changes in its public API before being considered stable.
> Please use at your own risk, and file Issues for any problems you may encounter.

## Overview

Ballast Scheduler is a lightweight way to reliably process a background, persistent job queue. This Core module is
completely independent of Ballast's MVI system, and focuses on the specific problem of enqueuing and running jobs, and
can be used without adopting the full MVI architecture.

This module provides the low-level infrastructure necessary to serialize tasks and store them in a persistent queue, to
be executed later. In general, this queue system supports multiple named queues, automatic retries (with configurable
backoff strategies), job cancellation, job checkpoints in the form of state persisted between retries, and stored result
values. Other features like priority scheduling, unique jobs, or delayed job starts, may be implemented by the specific
queue driver implementation.

Ballast Queue is a multiplatform project, with semantics and safety guarantees suitable for both long-running
server-side jobs queues meant to process large volumes of tasks, and also client-side applications for tasks such as 
synchronizing local data with a server.

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

- [Ballast Queue Viewmodel](./../ballast-queue-viewmodel)
- [Ballast Queue Exposed Driver](./../ballast-queue-exposed-driver)

## Usage

Ballast Queue is a layered system for running queues. It couples a low-level `QueueDriver`, which implements the basic
functions of enqueueing and dequeueing jobs based on pure data. A  `QueueExecutor` wraps a driver and adds higher-level
functionality for handling errors, type-safe job classes with automatic (de)serialization, and cancellation support.
You can then wrap the Executor in the common Ballast ViewModel interface with [Ballast Queue Viewmodel](./../ballast-queue-viewmodel)
so you can keep the same familiar syntax and semantics for processing persistent jobs that you already use for building
UI components.

### Overview

#### QueueDriver

The Driver is a very low-level component, and should not be used directly from application code. Its purpose is to allow
different job queue backends to be used by Ballast. Currently, Ballast supports an in-memory driver for quick
experimentation, and a synchronous driver suitable for end-to-end testing. The [Ballast Queue Exposed Driver](./../ballast-queue-exposed-driver)
module adds support for storing jobs in a database table, and currently supports PostgreSQL and MySQL database engines.

#### QueueExecutor

The Queue Executor is what you will be using to interact with your queue, as it provides a type-safe interface for
processing your jobs, and additional necessary functionality that is not suitable for the Driver.

#### Processing Loop

Ballast jobs are simple data classes which get serialized to JSON by the `QueueExecutor` and stored in a `QueueDriver`.
The Driver then sets up a processing loop as a Flow, which emits values back to the Executor when a job is ready to be
processed. The executor then deserializes that JSON payload back to its original data class, and calls a lambda for you
to handle the job execution. That execution can store intermediate state, which will be maintained if the job fails and
needs to be retried. Jobs can also return a result if it runs to completion successfully.

### Setting up a Queue

#### Step 1: Select a Driver

First, create an instance of your queue driver. The driver should be a singleton in your application.

Currently, the following drivers are available:

- **InMemoryQueueDriver**: The In-memory Queue Driver is a simple implementation of a QueueDriver that keeps all jobs in
  a list in memory, held in a `StateFlow` for observing the state of the queue and its jobs. This is primarily useful
  for testing and debugging, as its jobs are NOT persisted between application restarts.
- **SyncQueueDriver**: The Sync Queue Driver is a implementation of a QueueDriver that is intended for unit testing. It
  does not actually keep a queue of jobs, but instead uses a `RENDEZVOUS` `Channel` to immediately process the job
  synchronously. This allows you to have guarantees in your unit tests that calling `addToQueue` will process the job
  before returning, as long as another coroutine is currently observing the queue.
- **ExposedDatabaseQueueDriver**: The Exposed driver stores jobs in a database table, and uses the [Kotlin Exposed](https://www.jetbrains.com/exposed/)
  library to query that database. The table schema is designed for concurrency and safety of jobs, since it's meant to
  be used in a server-side application. See the [Ballast Queue Exposed Driver](./../ballast-queue-exposed-driver) documentation
  for more details on using this driver.

#### Step 2: Set up an Executor

The Executor provides a type-safe interface to the lower-level, untyped driver. It requires 4 generic type parameters:

- **Payload**: The Payload is a simple data class which defines the actual work to be done. It should generally contain
  only the minimal info necessary to run the jobs, such as an ID to a database record which needs to be processed. You
  may set up your queues with a single data class, or a `sealed class` to have one queue capable of enqueuing and
  dispatching multiple types of jobs.
- **State**: Jobs are able to maintain internal state which is only visible to that job. If a job fails and is retried,
  the state updates from the first run will be maintained, and the re-run will start with that state. This should be
  used primarily for building a system of "checkpoints" in the processing of a job, so retries don't need to be started
  from the beginning every time. It can also be used to report progress to an observer.
- **Result**: A job that runs to completion successfully is able to return a result. The library itself does not make
  use of this value, but your application logic may use it to store a report of what was processed, or temporarily store
  data that needs to be passed to another job.
- **JobMetadata**: This is the connector between your job and the underlying driver. Unlike many other job queue
  systems, Ballast does not try to implement all possible queueing logic in the primary interface, since the semantics
  of queues, and thus the data needed to configure the queue, can be significantly different between server-side and
  client-side use cases. Instead, it allows the Driver to define its own configuration, retry policies, etc. through a
  metadata object derived from the Payload. You are responsible for converting the Payload to the correct JobMetadata
  needed by the driver by implementing an instance of `QueueDriver.Adapter`. Each Driver should also include a generic
  `DefaultAdapter` which only uses default values and does not require any special configuration.

In addition to the type parameters, you will also need to provide a class to handle serialization and deserialization
of those types, by implementing `QueueExecutor.Serializers`. Support for [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
with JSON is provided out-of-the-box.

Example:

```kotlin
val driver = InMemoryQueueDriver(clock)
val executor = DefaultQueueExecutor(
    driver = driver,
    adapter = InMemoryQueueDriver.DefaultAdapter(),
    serializers = JsonSerializers(
        payloadSerializer = Payload.serializer(),
        resultSerializer = Result.serializer(),
        stateSerializer = State.serializer(),
        json = Json { prettyPrint = true },
    ),
)
```

#### Step 3: Enqueue jobs

Jobs are always inserted into a specific named queue. Queues with different names are treated as completely independent
entities. Jobs co-exist in the same storage, but are logically partitioned by their queue name. The queue name is just
an arbitrary String with no restrictions on name or format, but common names are `high`, `default`, `low` for defining
jobs of varying importance, and `dlq` for a "dead-letter queue".

Enqueueing a job is done with `executor.insertJob()`, which requires a `Payload` and an initial `State`. It returns a
String of the unique ID of the job, generated by the driver implementation.

Example:

```kotlin
val executor = DefaultQueueExecutor(
    // see Step 2
)
val uuid = executor.insertJob(
    queueName = "one",
    payload = TestPayload("ballast"),
    initialState = TestState(),
)
```

#### Step 4: Run the queue

The Queue is then run by calling `executor.runQueue()` and collecting the resulting Flow. The Executor itself is
stateless, so you can use the same Executor instance to run multiple Queues in parallel. A single Flow processes jobs
sequentially, one at a time (as is normal for un-buffered Flows). If you wish to increase the parallelism of a single
queue, you can simply repeat `executor.runQueue()` with the same Queue Name and collect each flow in a separate
Coroutine.

Normally, with Flows, the upstream Flow emits a value that is then processed by your downstream collector. In this case,
though, Ballast needs to perform some processing both _before_ and _after_ receiving a job from the driver. As a result,
you are required to pass in a lambda to `executor.runQueue()` to perform the task of processing a single job, and the
Flow returned actually emits _after_ a job has been processed, returning the result to the downstream collector. This
value may safely be ignored as it is already handled internally, but you may choose to inspect the job result for things
like logging or sending notifications on failure.

Additionally, since the collection of this Flow completely controls the lifetime of the queue processor, you are able to
run it indefinitely as a daemon, or only collect a certain number of jobs before quitting. For example, you may instead
process jobs in batches, during specific times, etc.

Example:

```kotlin
val executor = DefaultQueueExecutor(
    // see Step 2
)
val oneJob = executor
    .runQueue("one", ::processJob)
    .first()
```

#### Step 5: Processing the job with state

The `processJob` lambda is suspending, and is provided with a `QueueExecutorScope<State>` receiver, which gives you a
handle to get and update the `State` of the job during execution.

The first time a job is run, `scope.getCurrentState()` will return the initial State submitted to the queue with
`executor.insertJob()`. Within the same run, you can then call `scope.setState()` to update the Job record with a new
version of the state. This call will be applied synchronously to the Driver, so you have a guarantee that the state was
either persisted successfully if this call returns successfully, or else could not be applied for some reason, which
should throw an exception and fail the execution of this job. If the job failed and is retried, calling
`scope.getCurrentState()` in the subsequent run will instead return the last state successfully saved with
`scope.setState()`.

Consider this example how this State can help in designing a durable workflow with a Ballast Job. Imagine we have a
system where a user uploads an MP3 file to publish a podcast. Our system needs to transcode this MP3 to several
different bitrates, send the file to a AI cloud vendor to generate a transcript, and send notifications to subscribers.
Each of these operations can take a significant amount of time, and may fail due to network issues, vendor downtime,
etc.

To make this workflow durable, we can use the State to track and optionally skip operations that have already completed,
so retries do not necessarily need to do all 3 operations.

```kotlin
data class State(
    val transcodingComplete: Boolean = false,
    val transcriptionComplete: Boolean = false,
    val notificationsSent: Boolean,
)

suspend fun QueueExecutorScope<State>.processJob(podcast: Mp3File) {
    if (!getCurrentState().transcodingComplete) {
        performTranscoding(podcast)
        updateState(getCurrentState().copy(transcodingComplete = true))
    }

    if (!getCurrentState().transcriptionComplete) {
        transcriptionService.generateTranscription(podcast)
        updateState(getCurrentState().copy(transcriptionComplete = true))
    }

    if (!getCurrentState().notificationsSent) {
        notificationService.notifySubscribers(podcast)
        updateState(getCurrentState().copy(notificationsSent = true))
    }
}
```

#### Step 6: Job Results

The `Result` type parameter on your `QueueExecutor` is the value your job can return when it runs to completion
successfully. It's fully optional — you can use `Unit` if there's nothing meaningful to return, or `null` if the job
processed but produced no output in a particular run.

**Returning a result from a job**

Your `processJob` lambda simply returns a `Result?`. Return a value to signal success and attach data to the completed
job record; return `null` to signal success without any result payload.

```kotlin
suspend fun QueueExecutorScope<State>.processJob(payload: TranscodeMp3File): TranscodeResult? {
    val mp3File = fileService.findFileByPath(payload.uploadFilePath)
        ?: throw JobFailureException(permanentlyFail = true)

    performTranscoding(mp3File)

    // return a result to record what the job produced
    return TranscodeResult(outputPath = mp3File.transcodedPath, durationSeconds = mp3File.durationSeconds)
}
```

The result value is serialized and stored by the driver alongside the job record, so it can be retrieved later for
audit purposes or to be passed on to a subsequent job.

**Inspecting results from the Flow**

`runQueue()` returns a `Flow<JobProcessingResult<Result>>`. Each emission from this Flow represents a single job that
has finished processing — successfully or not. The `JobProcessingResult` carries the job ID, how long processing took,
and a `JobCompletionResult` describing the outcome:

```kotlin
executor
    .runQueue("default", ::processJob)
    .onEach { jobResult ->
        when (val result = jobResult.result) {
            is JobCompletionResult.Success -> {
                logger.info("Job ${jobResult.jobId} succeeded in ${jobResult.processingTime}: ${result.resultData}")
            }
            is JobCompletionResult.Failure -> {
                logger.error("Job ${jobResult.jobId} failed after ${jobResult.processingTime}: ${result.cause.message}")
            }
            is JobCompletionResult.Timeout -> {
                logger.warn("Job ${jobResult.jobId} timed out after ${jobResult.processingTime}")
            }
            is JobCompletionResult.Cancelled -> {
                logger.warn("Job ${jobResult.jobId} was manually cancelled")
            }
        }
    }
    .launchIn(applicationCoroutineScope)
```

These emissions are already handled internally — the driver has already been updated with the outcome by the time each
value is emitted — so you are free to ignore the Flow entirely if you don't need to act on individual results.

### Dealing with errors

#### Processing Failure

Work is typically moved to a queue because it takes a long time, has a nonzero chance of failure, and does not need to 
be processed immediately. Designing your application to move these points of failure to a job will help you maintain a 
fast, responsive application, while ensuring critical operations are guaranteed to be run successfully, eventually. 
Notably, queues operate on a principle of _eventual consistency_. Work may not complete immediately, but you can have 
assurance that it will at least complete _eventually_, being retried if it fails to recover from those errors.

Ballast queues are designed to be safe against all kinds of failures, including:

- **Normal processing errors**: Exceptions thrown during the precessing of a job will be caught and logged, and the
  job scheduled for retry according to the driver's retry policy
- **Timeouts**: Background jobs are expected to be slow, but sometimes they take significantly longer to process than
  they should. For example, a dependent service may be running particularly slowly, or your application server has run
  out of memory and the job gets hung. In these cases, Ballast will enforce a timeout on the job, so if it takes too
  long, it will cancel the job, report the error, and allow other jobs to continue which may be able to process faster.
  The cancelled job will be scheduled for retry according to the driver's retry policy.
- **Cancellation**: In addition to cancellation due to timeouts, you can manually cancel a job. This will cancel the
  coroutine currently processing the job, ensuring prompt termination and cleanup of the job, and allow the next job to
  run. The cancelled job will be scheduled for retry according to the driver's retry policy.
- **Application crashes**: Server processes are never guaranteed, and may sometimes be shutdown without any opportunity
  for the application to close gracefully. In this case, any jobs that were claimed for processing will get stuck in the
  "running" state and ineligible for retry, which is obviously not an acceptable solution. When a job is claimed from
  the driver, it is given a "lease" on that job for a short period of time (typically the timeout duration of the job,
  plus a short buffer ~30 seconds). In the case of a server crash, this lease will eventually expire and allow the job
  to be retried.

For cases of job exceptions or cancellation/timeouts, the job will immediately be released back to the queue for retries
according to the job's retry policy. This phrase is intentionally vague, as Ballast enforces no retry policy on its own,
and instead leaves the Queue Driver to define how and when to retry the job, and structure its `JobMetadata` to let each
job configure that policy on its own. For example, the `ExposedDatabaseQueueDriver` allows jobs to be retried based on
the number of attempts or will retry as many times as it needs until a specified expiry time is exceeded. The 
`InMemoryQueueDriver` only supports retries based on the number of attempts. Other queue systems, like Amazon SQS, may 
include their own policies, and Ballast will simply notify the driver of the failure and it figure out whether it should
retry or not.

#### Retry Backoff

When a job fails and may need to be retried, it can be given a delay as a buffer against temporal issues. A default
retry for all jobs in the queue can be set in the `DriverQueue.Adapter.getDefaultRetryDelayTimeout()`, which can be
configured individually for each payload. This method is also provided the number of times the job has already been
attempted, so it can be used for increasing the backoff delay after each attempt. See example backoff strategies below:

```kotlin
public fun getDefaultRetryDelayTimeout(payload: Unit, attempts: Int): Duration {
    // exponential backoff: 2^attempts in minutes, to a maximum of 1 hour
    return minOf((2.0.pow(attempts.toDouble()).toLong()).minutes, 60.minutes)
}

public fun getDefaultRetryDelayTimeout(payload: Unit, attempts: Int): Duration {
    // fixed array of increasing delays, in minutes
    val delays = listOf(1, 2, 5, 10, 30, 60, 90)
    val index = attempts.coerceAtMost(delays.size) - 1
    return delays[index].minutes
}
```

However, in some cases, a fixed retry delay is not always able to capture the real backoff needs, especially in the case
of calling a rate-limited API from an external webservice. These API endpoints return a specific number of seconds your
application must wait before requests will succeed, as a protection against DDoS attacks or as a way to meter API usage.

To use data from the job processing itself as the basis for a backoff delay, throw `JobFailureException` from your job
and set the `retryDelay`. See this example for catching errors from the webservice to determine the necessary delay:

```kotlin
suspend fun QueueExecutorScope<State>.processJob(podcast: Mp3File) {
    try {
        notificationService.notifySubscribers(podcast)
    } catch (e: HttpException) {
        if (e.statusCode == 429) {
            val retryAfter = Instant.parse(e.response.headers["Retry-After"])
            val now = clock.now()
            val delay = retryAfter - now
            throw JobFailureException(cause = e, retryDelay = delay)
        }
    }
}
```

#### Permanent failures and Dead-Letter Queues

Ballast does not enforce any specific concept of a "dead-letter queue" (DLQ) by itself. Like Retry Policies, it leaves
this functionality up to the driver. Functionally, a DLQ is no different from any other queue. It simply defines the
"Queue Name" of a queue, and an alternate mode of processing that usually just notifies system admins of the failure
rather than actually processing the job. So if your driver has a DLQ, you just need to collect from that queue by name.

Ballast does not automatically move jobs to a different DLQ, but instead would prefer to simply mark a job as
permanently failed and leave it in the original queue, ineligible to be claimed and processed. Should you need a DLQ, 
it is either up to the driver to move the job to a DLQ immediately when marking it as failed, or else periodically 
scanning the jobs store and manually moving the job to a DLQ.

Jobs are considered "permanently failed" if they fail during execution, and the queue does not permit an additional
retry. They are moved to a "failed" state which indicates the permanent failure, so you can query the queue to
appropriately deal with those failed jobs.

Sometimes, during the execution of a job, you can detect that the job will _never_ succeed, no matter how many times it
is retried. For example, an API token may have expired, a validation error in the job's Payload renders it
unprocessable, or the DB record that's supposed to be processed by the job has already been deleted. In these cases,
you'll want to mark the job as permanently failed immediately so Ballast does not attempt to retry that job, wasting
system resources. This is also done by throwing `JobFailureException` and setting `permanentlyFail = true`.

```kotlin
suspend fun QueueExecutorScope<State>.processJob(payload: TranscodeMp3File) {
    val mp3File = fileService.findFileByPath(payload.uploadFilePath)

    if (mp3File == null) {
        // oops, the file was already deleted
        throw JobFailureException(cause = e, permanentlyFail = true)
    }

    performTranscoding(mp3File)
}
```

### Rate-limiting

#### Concept

In the absence of any kind of rate-limiting, it would be very easy for an issue in your server to process jobs too
quickly and overwhelm other webservices.

Consider this example:

> You have a webservice which generates about 1,000 jobs per hour, which post data to a downstream API. You pay for a
> rate-limiting policy from that service which roughly matches the rate at which jobs are generated. Occasionally spikes
> in traffic will cause jobs to back up in the queue and be processed more slowly as that downstream API returns 429
> errors, but subsequent dips in traffic easily allow the queue to catch back up within a short time.
>
> However, an issue causes your queue processor to go down at the same time you receive a large spike in traffic. During
> the outage, you end up with more than 50,000 jobs in the queue. When the service comes back online, it starts
> processing those jobs as quickly as it can, a 50-fold increase in the normal rate of processing. As such, the
> downstream service starts applying aggressive rate-limiting policies as DDoS protection. This DDoS protection causes
> all the jobs in your queue to fail, getting enqueued for retry. Meanwhile, more jobs are continually being added. This
> cascade of failures and retries continually prevents your server from being able to access the downstream service, and
> you're never able to drain the queue. You're forced to take your application offline, wait for the 429 errors to
> subside, then restart the queue and process the jobs very slowly to allow the system to catch.
>
> In all, because the queue did not enforce its own rate-limiting behavior, the downstream service's rate-limiting
> kicked in to protected itself, which exacerbated the original problem, causing another outage in your application.

While the above scenario is obviously a bit fanciful, it is a real situation that one could get themselves in if care
isn't taken to protect your downstream services. This is where Ballast's `QueueThrottle` comes in.

In Ballast Queues, a "throttle" is a lightweight policy _shared among all queue workers_ which helps limit the overall
concurrency or rate of job processing by the entire system. Ballast offers several simple, yet effective, policies to
avoid processing jobs too quickly. The default policies all operate in-memory, protecting a single process, though you
can implement your own policies to share state among nodes in a distributed system (i.e. using Redis distributed locks).

Conceptually, Ballast Queues run a busy-loop in a coroutine. If a job is eligible for processing, it claims it,
processes the job, then stores the result. The loop is then repeated, and a delay is only applied to this loop if there
was no job available for processing. A `QueueThrottle`, therefore, adds a delay to that loop _before_ it checks for an
available job, suspending until the throttle permits the worker to try and claim a job.

#### Applying Throttling policies

Queue Throttles are intended to be created as a singleton, and passed into a supporting `QueueDriver`. The`QueuePolicy`
itself must be a singleton, shared by all workers and/or drivers of your application.

In this example, there are a total of 7 Workers each running in parallel, but the ConcurrencyLimitThrottle limits the 
queue to only 4 active jobs at a time amongst all 7 workers, regardless of the queue priority.

```kotlin
val executor = DefaultQueueExecutor(
    driver = InMemoryQueueDriver(
        throttle = ConcurrencyLimitThrottle(4),
    ),
    adapter = InMemoryQueueDriver.DefaultAdapter(),
    serializers = JsonSerializers(
        payloadSerializer = TestPayload.serializer(),
        resultSerializer = TestResult.serializer(),
        stateSerializer = TestState.serializer(),
    ),
)

listOf("high" to 4, "default" to 2, "low" to 1).forEach { (queueName, replicaCount) ->
    repeat(replicaCount) {
        executor
            .runQueue(queueName, ::proessJob)
            .launchIn(applicationCoroutineScope)
    }
}
```

#### Available Policies

By default, Ballast does not impose any rate-limiting, by using the `UnlimitedThrottle`, but you should ensure any
production workloads do select and apply an appropriate policy. You may choose from one of the below policies available, 
or implement a custom policy.

- **UnlimitedThrottle**: Applies no throttling to the queue. Not recommended for server-side workloads, but probably
  fine for low-volume client-side workloads.
- **ConcurrencyLimitThrottle**: Limits the workers to at most `N` jobs being actively processed concurrently.
- **TokenBucketThrottle**: A simple algorithm enforcing an upper-end on the rate of jobs. By continually filling a
  "bucket" at a constant rate, queues must wait for the bucket to fill before being allowed to claim and process a
  job. In low-volume scenarios, jobs will be processed as quickly as possible since the bucket will always have "tokens" 
  available, but as volume increases, jobs will only be processed at the rate at which "tokens" are added to the bucket.
- **PerQueueThrottle**: Delegate different throttling policies to each queue by name
- **CompositeThrottle**: Require multiple delegated policies to become available before a worker can claim a job.

These policies can be combined together, to create more complex policies. For example:

```kotlin
val totalSystemConcurrency = ConcurrencyLimitThrottle(4)

// 1 job per second, processing bursts of up to 10 jobs
val highRateLimit = TokenBucketThrottle(
    scope = applicationCoroutineScope,
    capacity = 10,
    refillRatePerTick = 1,
    tickDuration = 1.seconds,
)

// 1 job per minute, processing bursts of up to 4 jobs
val defaultAndLowRateLimit = TokenBucketThrottle(
    scope = applicationCoroutineScope,
    capacity = 4,
    refillRatePerTick = 2,
    tickDuration = 1.minutes,
)

val throttle = PerQueueThrottle(
    policies = mapOf(
        "high" to CompositeThrottle(totalSystemConcurrency, highRateLimit),
        "default" to CompositeThrottle(totalSystemConcurrency, defaultAndLowRateLimit),
        "low" to CompositeThrottle(totalSystemConcurrency, defaultAndLowRateLimit),
    ),
    default = totalSystemConcurrency
)
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-queue-core:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-queue-core:{{ballastVersion}}")
            }
        }
    }
}
```
