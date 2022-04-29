---
---

Ballast offers several logger implementations out-of-the-box:

- `NoOpLogger`: The default implementation, it simply drops all messages and exceptions so nothing gets logged
  accidentally. It's recommended to use this in production builds, as well, and using [Ballast Firebase][1] to control
  what actually gets logged in production.
- `PrintlnLogger`: Useful for quick-and-dirty logging on all platforms. It just writes log messages to stdout through
  println.
- `AndroidBallastLogger`: Only available on Android, writes logs to the default LogCat at the appropriate levels.

[1]: {{ 'Ballast Firebase' | link }}
