---
---

# {{ page.title }}

Ballast supports WasmJS targets since verion 4.2.0, and has been tested in Compose Web applications. See 
{{ 'Compose' | anchor }} for details on integrating Ballast into a Compose Web application. Please note the following 
limitations of Ballast in Ballast 4.2.0:

- Only `wasmJs` is supported. `wasmWasi` target is not currently supported due to lack of support from kotlinx.coroutines
- `:ballast-debugger-client` does not support `wasmJs`, because stable builds of Ktor Client don't support `wasmJs` yet.
- `:ballast-firebase-analytics` and `:ballast-firebase-crashlytics` do not support any targets other than Android, thus 
  these modules are not available on `wasmJs`. However, the more generic version of those modules, `:ballast-analytics` 
  and `:ballast-crash-reporting` are supported on `wasmJs`.
- All other Ballast modules do support `wasmJs` targets, including `:ballast-navigation`. 
