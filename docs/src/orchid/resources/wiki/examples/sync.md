---
extraJs:
  - 'assets/example/distributions/mpp.js'
---

# {{ page.title }}

This example uses the UI of the Basic Counter example, but synchronizes that state across multiple instances of that 
ViewModel and UI. This example adds a short delay between each synchronzied change so you can better understand how the
data flows between all the ViewModels.

<div id="example_sync"></div>
<br><br>

{% snippet 'debuggerProTip' %}
