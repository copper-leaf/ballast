## 0.7.0 - 2022-02-14

- Adds Repository module for app-wide caching and communication

## 0.6.0 - 2022-01-27

- Improves DSL for restarting SideEffects

## 0.5.0 - 2022-01-16

- Fixes issue with input Channel buffer that didn't do exactly what I expected

## 0.4.0 - 2022-01-14

- Tweaks API a bit
- SideEffects must now be the last blocks called while handling an Input, to avoid confusion about dispatching the side
  effect but not actually running until the Input processing has completed

## 0.3.0 - 2022-01-06

- Adds different strategies for accepting and processing inputs to allow for other use-cases.

- ## 0.2.0 - 2022-01-05

- Adds `ballast-test`

## 0.1.0 - 2021-11-18

- Initial Commit
