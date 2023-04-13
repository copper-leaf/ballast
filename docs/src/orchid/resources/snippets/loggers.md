---
---

Ballast offers several logger implementations out-of-the-box:

- `NoOpLogger`: The default implementation, it simply drops all messages and exceptions so nothing gets logged
  accidentally. It's recommended to use this in production builds.
- `PrintlnLogger`: Useful for quick-and-dirty logging on all platforms. It just writes log messages to stdout through
  println.
- `AndroidLogger`: Only available on Android, writes logs to the default LogCat at the appropriate levels.
- `JsConsoleLogger`: Only available on JS, writes logs to `console.log()` or `console.error()`
- `NSLogLogger`: Only available on iOS, writes logs to `NSLog`
- `OSLogLogger`: Only available on iOS, writes logs to `OSLog`
