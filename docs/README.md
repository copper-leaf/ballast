# Ballast Documentation

Ballast is an opinionated MVI state management framework for Kotlin Multiplatform. This directory contains
project-level documentation. Module-specific documentation lives in each module's own README.

## In This Directory

- [Getting Started](getting-started.md) — Build your first Ballast screen step by step
- [Feature Overview](feature-overview.md) — Core concepts: ViewModels, Contracts, Handlers, Side Jobs, Interceptors
- [Thinking in Ballast MVI](mental-model.md) — Deep dive into the MVI model, state design philosophy, and Ballast's approach
- [Feature Comparison](feature-comparison.md) — Ballast vs Redux, Orbit, MVIKotlin, Uniflow-kt
- [Community](community.md) — Community-built extensions and integrations

### Migration Guides

Ballast has had a number of major version releases over the years. While the core APIs have remained stable, there are
some notable changes from time-to-time that you'll need to keep up with. Whenever possible, breaking changes are first
marked as deprecated for at least 1 full major-version cycle so you have ample time to migrate to its replacement before
the legacy functionality is removed.

- [v2 → v3](migration/v3.md)
- [v3 → v4](migration/v4.md)

---

## Modules

### Core

The `ballast-core` module is the main dependency you'll need to get started with Ballast. It brings in all of the 
following sub-modules:

| Module                                     | Description                                                                    |
|--------------------------------------------|--------------------------------------------------------------------------------|
| [ballast-core](../ballast-core/)           | **Start here.** Aggregates the core modules; standard dependency for most apps |
| [ballast-api](../ballast-api/)             | Core interfaces and contracts; use this when building Ballast extensions       |
| [ballast-viewmodel](../ballast-viewmodel/) | Platform-specific ViewModel base classes (Android, iOS, Basic)                 |
| [ballast-logging](../ballast-logging/)     | Logging interceptor and platform-specific logger implementations               |
| [ballast-utils](../ballast-utils/)         | Internal utilities used by other Ballast modules                               |

### Front-end Features

Ballast is designed with a flexible plugin architecture. The following modules provide plugins to augment the core MVI
functionality with useful features for UI development

| Module                                                                               | Description                                                                       |
|--------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| [ballast-saved-state](../ballast-saved-state/)                                       | Save and restore ViewModel state across process death                             |
| [ballast-undo](../ballast-undo/)                                                     | Undo/redo support via state snapshots                                             |
| [ballast-sync](../ballast-sync/)                                                     | Synchronize state across multiple ViewModel instances                             |
| [ballast-analytics](../ballast-analytics/)                                           | Analytics event tracking interceptor                                              |
| [ballast-firebase-analytics](../ballast-firebase-analytics/)                         | Firebase Analytics tracker for `ballast-analytics`                                |
| [ballast-crash-reporting](../ballast-crash-reporting/)                               | Crash reporting interceptor                                                       |
| [ballast-firebase-crashlytics](../ballast-firebase-crashlytics/)                     | Firebase Crashlytics reporter for `ballast-crash-reporting`                       |
| [ballast-navigation](../ballast-navigation/)                                         | Type-safe navigation and backstack management                                     |
| [ballast-repository](../ballast-repository/)                                         | **Deprecated** MVI pattern extended to the repository layer with built-in caching |
| [ballast-schedules](../ballast-schedules/)                                           | **Deprecated** Schedule definitions for use with the scheduler modules            |
| [ballast-scheduler-core](../ballast-scheduler-core/)                                 | Core scheduler infrastructure                                                     |
| [ballast-scheduler-viewmodel](../ballast-scheduler-viewmodel/)                       | ViewModel-based scheduler                                                         |
| [ballast-scheduler-android-alarmmanager](../ballast-scheduler-android-alarmmanager/) | Android AlarmManager-based scheduler                                              |

### Server-side Features

Recently, Ballast has evolved beyond just front-end state management, and is becoming a solution for server-side 
event-driven workloads.

| Module                                                           | Description                                                               |
|------------------------------------------------------------------|---------------------------------------------------------------------------|
| [ballast-ktor-server](../ballast-ktor-server/)                   | Ktor server-side integration                                              |
| [ballast-autoscale](../ballast-autoscale/)                       | Automatically scale ViewModel resources based on load                     |
| [ballast-queue-core](../ballast-queue-core/)                     | Persistent job queue core                                                 |
| [ballast-queue-viewmodel](../ballast-queue-viewmodel/)           | ViewModel-based job queue                                                 |
| [ballast-queue-exposed-driver](../ballast-queue-exposed-driver/) | Exposed (SQL) storage driver for the job queue                            |
| [ballast-scheduler-core](../ballast-scheduler-core/)             | Core scheduler infrastructure                                             |
| [ballast-scheduler-viewmodel](../ballast-scheduler-viewmodel/)   | ViewModel-based scheduler. Integrated well with the ViewModel-based queue |
| [ballast-scheduler-cron](../ballast-scheduler-cron/)             | Cron expression support for the scheduler                                 |

### Utilities

These utilities help you in the development and maintenance of your Ballast ViewModels.

| Module                                                             | Description                                                           |
|--------------------------------------------------------------------|-----------------------------------------------------------------------|
| [ballast-test](../ballast-test/)                                   | Testing utilities for Ballast ViewModels                              |
| [ballast-kotlinx-serialization](../ballast-kotlinx-serialization/) | kotlinx.serialization support for debugger and other modules          |
| [ballast-debugger-client](../ballast-debugger-client/)             | Interceptor that connects ViewModels to the IntelliJ debugger UI      |
| [ballast-debugger-models](../ballast-debugger-models/)             | Shared data models for debugger client/server communication           |
| [ballast-idea-plugin](../ballast-idea-plugin/)                     | IntelliJ plugin — real-time ViewModel inspection and code scaffolding |

---

## Examples

There are many examples showing how to use the various plugins and features of Ballast. Clone this repo and run these 
examples to better understand Ballast's many features.

| Example                                                           | Description                                                                            |
|-------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| [counter](../examples/counter/)                                   | Minimal counter — the simplest possible Ballast app                                    |
| [navigationWithEnumRoutes](../examples/navigationWithEnumRoutes/) | Navigation and backstack management with enum-defined routes                           |
| [web](../examples/web/)                                           | JS/browser app with multiple scenarios: Kitchen Sink, ScoreKeeper, Sync, Undo, BGG API |
| [android](../examples/android/)                                   | Android implementations of the same scenarios as the web example                       |
| [desktop](../examples/desktop/)                                   | Compose Desktop implementations of the same scenarios as the web example               |
| [compose_sharedui_kmm](../examples/compose_sharedui_kmm/)         | Shared Compose UI across Android, iOS, Desktop, and Web                                |
| [queue](../examples/queue/)                                       | Job queue example                                                                      |
