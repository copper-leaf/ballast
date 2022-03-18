---
---

# Platforms officially supported by Ballast

Ballast was intentionally designed to not be tied directly to any particular platform or UI toolkit. In fact, while most
Kotlin MVI libraries were initially developed for Android and show many artifacts of that initial base, Ballast started
as a State Management solution for Compose Desktop.

Because Ballast was initially designed entirely in a non-Android context, it should work in any Kotlin target or 
platform as long as it works with Coroutines and Flows. However, the following targets are officially supported, in 
that they have been tested and are known to work there, or have specific features for that platform

- {{ 'Android' | anchor }}
- {{ 'iOS' | anchor }} (requires new Kotlin Native Memory Model)
- {{ 'Compose Desktop' | anchor }}
- {{ 'Compose Web' | anchor }}
