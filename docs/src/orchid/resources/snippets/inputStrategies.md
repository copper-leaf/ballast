---
---

Ballast offers 3 different Input Strategies out-of-the-box, which each adapt Ballast's core functionality for different
applications:

- `LifoInputStrategy`: A last-in-first-out strategy for handling Inputs, and the default strategy if none is provided.
  Only 1 Input will be processed at a time, and if a new Input is received while one is still working, the running Input
  will be cancelled to immediately accept the new one. Corresponds to `Flow.collectLatest { }`, best for UI ViewModels.
- `FifoInputStrategy`: A first-in-first-out strategy for handling Inputs. Inputs will be processed in the same order
  they were sent and only ever one-at-a-time, but instead of cancelling running Inputs, new ones are queued and will be
  consumed later when the queue is free. Corresponds to the normal `Flow.collect { }`, best for non-UI ViewModels.
- `ParallelInputStrategy`: For specific edge-cases where neither of the above strategies works. Inputs are all handled
  concurrently so you don't have to worry about blocking the queue or having Inputs cancelled. However, it places
  additional restrictions on State reads/changes to prevent usage that might lead to race conditions.

InputStrategies are responsible for creating the Channel used to buffer incoming Inputs, consuming the Inputs from that 
channel, and providing a "Guardian" to ensure the Inputs are handled properly according the needs of that particular 
strategy. The `DefaultGuardian` is a good starting place if you need to create your own `InputStrategy` to
maintain the same level of safety as the core strategies listed above.

{% alert 'info' :: compileAs('md') %}
Pro Tip: The text descriptions of these InputStrategies can be a bit confusing, but seeing them play out in real-time
should make it obvious how they work. Playing with the [Kitchen Sink][1] with the [Debugger][2] gives you a simple way
of experiencing these behaviors to get an intuition for when to use each one.

[1]: {{ 'Kitchen Sink' | link }}
[2]: {{ 'Ballast Debugger' | link }}
{% endalert %}
