---
---

# High-level Feature Overview

## Inputs

Inputs are the most central mechanism of how Ballast works. The "intent" a user has when interacting with the UI is
captured into an Input, which is sent to the Ballast ViewModel and processed at some point in time after that, where
they have easy access to the entire UI state at the point in time they are processed (not when they were created). This
is in contrast to normal UI programming, where callbacks execute business logic directly and immediately, without regard
to what other code might be running at that time.

## State

## Events

## Side Effects

## Interceptors

## Input Strategies
