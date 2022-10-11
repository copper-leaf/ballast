---
extraJs:
  - 'assets/example/distributions/mpp.js'
---

# {{ page.title }}

This example shows how the [Undo/Redo][1] functionality works. As you enter text into the text field, the ViewModel
State will be captured once every 5 seconds. After multiple changes have been made, you will be able to use the 
undo/redo buttons to navigate back through the previous edits.

<div id="example_undo"></div>
<br><br>

{% snippet 'debuggerProTip' %}

[1]: {{ 'Ballast Undo' | link }}
