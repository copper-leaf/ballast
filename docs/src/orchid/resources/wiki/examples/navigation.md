---
extraJs:
  - 'assets/examples/web/web.js'
---

# {{ page.title }}

This example demonstrates Ballast Navigation, with all the other examples shown in a tabbed view. It also uses the 
`BrowserHashNavigationInterceptor` to synchronize the Router state with the browser's address bar. Changes to the app 
state will update the URL hash, and you may manually edit or hyperlink to a page at a given hash to load that tab.

The Router also integrates with all other Ballast features, such as Sync, Undo, or the Debugger. Try opening the 
Debugger to watch the Router state get updated!

<div id="examples_navigation"></div>
<br>

#### Sources:

- [Android](https://github.com/copper-leaf/ballast/tree/main/examples/android/src/androidMain/java/com/copperleaf/ballast/examples/router)
- [Compose Desktop](https://github.com/copper-leaf/ballast/tree/main/examples/desktop/src/jvmMain/kotlin/com/copperleaf/ballast/examples/router)
- [Compose Web](https://github.com/copper-leaf/ballast/tree/main/examples/web/src/jsMain/kotlin/com/copperleaf/ballast/examples/router)

{% snippet 'debuggerProTip' %}

[1]: {{ 'Ballast Undo' | link }}
