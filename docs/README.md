# Ballast Documentation

Ballast is an opinionated MVI state management framework for Kotlin Multiplatform. This directory contains
project-level documentation. Module-specific documentation lives in each module's own README.

## In This Directory

- [Feature Overview](feature-overview.md) — Core concepts: ViewModels, Contracts, Handlers, Side Jobs, Interceptors
- [Thinking in Ballast MVI](mental-model.md) — Deep dive into the MVI model, state design philosophy, and Ballast's approach
- [Feature Comparison](feature-comparison.md) — Ballast vs Redux, Orbit, MVIKotlin, Uniflow-kt
- [Community](community.md) — Community-built extensions and integrations

### Migration Guides

- [v2 → v3](migration/v3.md)
- [v3 → v4](migration/v4.md)

---

## Modules

### Core

| Module | Description |
|--------|-------------|
| [ballast-core](../ballast-core/) | **Start here.** Aggregates the core modules; standard dependency for most apps |
| [ballast-api](../ballast-api/) | Core interfaces and contracts; use this when building Ballast extensions |
| [ballast-viewmodel](../ballast-viewmodel/) | Platform-specific ViewModel base classes (Android, iOS, Basic) |
| [ballast-logging](../ballast-logging/) | Logging interceptor and platform-specific logger implementations |
| [ballast-utils](../ballast-utils/) | Internal utilities used by other Ballast modules |

### Features

| Module | Description |
|--------|-------------|
| [ballast-navigation](../ballast-navigation/) | Type-safe navigation and backstack management |
| [ballast-repository](../ballast-repository/) | MVI pattern extended to the repository layer with built-in caching |
| [ballast-saved-state](../ballast-saved-state/) | Save and restore ViewModel state across process death |
| [ballast-undo](../ballast-undo/) | Undo/redo support via state snapshots |
| [ballast-sync](../ballast-sync/) | Synchronize state across multiple ViewModel instances |
| [ballast-test](../ballast-test/) | Testing utilities for Ballast ViewModels |
| [ballast-analytics](../ballast-analytics/) | Analytics event tracking interceptor |
| [ballast-crash-reporting](../ballast-crash-reporting/) | Crash reporting interceptor |
| [ballast-autoscale](../ballast-autoscale/) | Automatically scale ViewModel resources based on load |

### Firebase Integrations

| Module | Description |
|--------|-------------|
| [ballast-firebase-analytics](../ballast-firebase-analytics/) | Firebase Analytics tracker for `ballast-analytics` |
| [ballast-firebase-crashlytics](../ballast-firebase-crashlytics/) | Firebase Crashlytics reporter for `ballast-crash-reporting` |

### Serialization & Networking

| Module | Description |
|--------|-------------|
| [ballast-kotlinx-serialization](../ballast-kotlinx-serialization/) | kotlinx.serialization support for debugger and other modules |
| [ballast-ktor-server](../ballast-ktor-server/) | Ktor server-side integration |

### Debugger

| Module | Description |
|--------|-------------|
| [ballast-debugger-client](../ballast-debugger-client/) | Interceptor that connects ViewModels to the IntelliJ debugger UI |
| [ballast-debugger-models](../ballast-debugger-models/) | Shared data models for debugger client/server communication |
| [ballast-idea-plugin](../ballast-idea-plugin/) | IntelliJ plugin — real-time ViewModel inspection and code scaffolding |

### Scheduler

| Module | Description |
|--------|-------------|
| [ballast-schedules](../ballast-schedules/) | Schedule definitions for use with the scheduler modules |
| [ballast-scheduler-core](../ballast-scheduler-core/) | Core scheduler infrastructure |
| [ballast-scheduler-viewmodel](../ballast-scheduler-viewmodel/) | ViewModel-based scheduler |
| [ballast-scheduler-cron](../ballast-scheduler-cron/) | Cron expression support for the scheduler |
| [ballast-scheduler-android-alarmmanager](../ballast-scheduler-android-alarmmanager/) | Android AlarmManager-based scheduler |

### Job Queue

| Module | Description |
|--------|-------------|
| [ballast-queue-core](../ballast-queue-core/) | Persistent job queue core |
| [ballast-queue-viewmodel](../ballast-queue-viewmodel/) | ViewModel-based job queue |
| [ballast-queue-exposed-driver](../ballast-queue-exposed-driver/) | Exposed (SQL) storage driver for the job queue |

---

## Examples

| Example | Description |
|---------|-------------|
| [counter](../examples/counter/) | Minimal counter — the simplest possible Ballast app |
| [navigationWithEnumRoutes](../examples/navigationWithEnumRoutes/) | Navigation and backstack management with enum-defined routes |
| [web](../examples/web/) | JS/browser app with multiple scenarios: Kitchen Sink, ScoreKeeper, Sync, Undo, BGG API |
| [android](../examples/android/) | Android implementations of the same scenarios as the web example |
| [desktop](../examples/desktop/) | Compose Desktop implementations of the same scenarios as the web example |
| [compose_sharedui_kmm](../examples/compose_sharedui_kmm/) | Shared Compose UI across Android, iOS, Desktop, and Web |
| [queue](../examples/queue/) | Job queue example |
