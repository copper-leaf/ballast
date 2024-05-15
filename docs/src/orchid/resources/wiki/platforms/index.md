---
---

# {{ page.title }}

Ballast was intentionally designed to not be tied directly to any particular platform or UI toolkit. In fact, while most
Kotlin MVI libraries were initially developed for Android and show many artifacts of that initial base, Ballast started
as a State Management solution for Compose Desktop and intentionally avoids any terminology or APIs that are really only
useful as an Android feature. Anything built for Ballast is expected to work on all platforms.

Because Ballast was initially designed entirely in a non-Android context, it should work in any Kotlin target or 
platform as long as it works with Coroutines and Flows. However, the following targets are officially supported, in 
that they have been tested and are known to work there, or have specific features for that platform.

- {{ 'Android' | anchor }}
- {{ 'Compose' | anchor }}
- {{ 'SwiftUI' | anchor }}
- {{ 'WasmJS' | anchor }}
- {{ 'KVision' | anchor }}
