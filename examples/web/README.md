# Web Examples

## Overview

A JS/browser application built with Compose HTML that hosts several Ballast example scenarios. The same scenarios are
also available as [Android](../android) and [Desktop](../desktop) targets.

## Running Locally

```shell
./gradlew :examples:web:jsBrowserDevelopmentRun
```
Then open http://localhost:8080

## Examples

### Counter

A minimal counter demonstrating the bare basics of sending Inputs and updating ViewModel State. Good first example to
understand the core MVI loop.

Sources: [web](src/jsMain/kotlin/com/copperleaf/ballast/examples/ui/counter)

---

### Kitchen Sink

Demonstrates the usage of all of Ballast's core APIs in one place: Inputs, Events, Side Jobs, and multiple Input
Strategies. Most useful when run alongside the [Ballast Debugger](./../../ballast-debugger-client) so you can watch
the activity in real-time as the various features run.

Sources: [web](src/jsMain/kotlin/com/copperleaf/ballast/examples/ui/kitchensink)

---

### ScoreKeeper

A more complex counter that manages a list of players and their scores. Demonstrates:

- Managing a list of items in State
- Delayed State commits (scores are previewed for 5 seconds before being finalized)
- Persistent storage using [Ballast Saved State](./../../ballast-saved-state) — scores are saved to LocalStorage and
  restored on page reload

Sources: [web](src/jsMain/kotlin/com/copperleaf/ballast/examples/ui/scorekeeper)

---

### Sync

Uses the Counter UI but synchronizes State across multiple independent ViewModel instances using
[Ballast Sync](./../../ballast-sync). A short delay between synchronized changes makes the data flow between
ViewModels visible.

Sources: [web](src/jsMain/kotlin/com/copperleaf/ballast/examples/ui/sync)

---

### Undo/Redo

Shows how [Ballast Undo](./../../ballast-undo) works. As you type into a text field, the ViewModel State is
snapshotted every 5 seconds. After multiple changes, the undo/redo buttons let you navigate back through previous
edits.

Sources: [web](src/jsMain/kotlin/com/copperleaf/ballast/examples/ui/undo)

---

### BGG API & Cache

Shows how to make and cache API calls using [Ballast Repository](./../../ballast-repository). Fetches from the
[BoardGameGeek XML API v2](https://boardgamegeek.com/wiki/page/BGG_XML_API2) and caches the response in-memory.
The cache is returned on subsequent fetches unless "Force Refresh" is checked or the selected hotlist type changes.

Sources: [web](src/jsMain/kotlin/com/copperleaf/ballast/examples/ui/bgg)
