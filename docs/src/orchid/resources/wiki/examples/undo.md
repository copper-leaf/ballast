---
extraJs:
  - 'assets/examples/web/web.js'
---

# {{ page.title }}

This example shows how the [Undo/Redo][1] functionality works. As you enter text into the text field, the ViewModel
State will be captured once every 5 seconds. After multiple changes have been made, you will be able to use the 
undo/redo buttons to navigate back through the previous edits.

<div id="example_undo"></div>
<br>

#### Sources:

- [Android](https://github.com/copper-leaf/ballast/tree/main/examples/android/src/androidMain/java/com/copperleaf/ballast/examples/ui/undo)
- [Compose Desktop](https://github.com/copper-leaf/ballast/tree/main/examples/desktop/src/jvmMain/kotlin/com/copperleaf/ballast/examples/ui/undo)
- [Compose Web](https://github.com/copper-leaf/ballast/tree/main/examples/web/src/jsMain/kotlin/com/copperleaf/ballast/examples/ui/undo)

{% snippet 'debuggerProTip' %}

[1]: {{ 'Ballast Undo' | link }}
