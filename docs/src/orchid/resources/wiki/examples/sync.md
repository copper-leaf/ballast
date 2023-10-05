---
extraJs:
  - 'assets/examples/web/web.js'
---

# {{ page.title }}

This example uses the UI of the Basic Counter example, but synchronizes that state across multiple instances of that 
ViewModel and UI. This example adds a short delay between each synchronzied change so you can better understand how the
data flows between all the ViewModels.

<div id="example_sync"></div>
<br>

#### Sources:

- [Android](https://github.com/copper-leaf/ballast/tree/main/examples/android/src/androidMain/java/com/copperleaf/ballast/examples/ui/sync)
- [Compose Desktop](https://github.com/copper-leaf/ballast/tree/main/examples/desktop/src/jvmMain/kotlin/com/copperleaf/ballast/examples/ui/sync)
- [Compose Web](https://github.com/copper-leaf/ballast/tree/main/examples/web/src/jsMain/kotlin/com/copperleaf/ballast/examples/ui/sync)

{% snippet 'debuggerProTip' %}
