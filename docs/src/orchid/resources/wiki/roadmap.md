---
---

# Ballast Roadmap

The core Ballast functionality is mostly stable and will not change much after 1.0.0. But the Interceptor API is robust
and offers a lot of possibilities for extending the functionality of your applications. The following are some ideas
of what could be done with Ballast and its Interceptor API, though nothing on this list is an actual hard commitment to
that it will be developed. 

If you see an idea on this like you want, by all means, go build it yourself! I will gladly accept any contribution
as a PR, or if you would rather develop the feature in a separate repo I will happily link to that repo from the Ballast
documentation.

- [Synchrony Module][#2]: Synchronize the VM state between multiple clients over a network connection, for building
  realtime multi-user applications
- [Finite State Machine DSL][#7]: Ballast is basically an FSM already, and it would be nice to build a dedicated DSL
  like [Tinder/StateMachine][a] on top of Ballast so it could be used on any Kotlin target
- [Undo/redo functionality][#10]: An undo/redo controller that tracks "frames" for when Inputs are processed, and move
  forward/backward through those frames to restore the ViewModel to that point in time 

[#2]: https://github.com/copper-leaf/ballast/issues/2
[#3]: https://github.com/copper-leaf/ballast/issues/3
[#7]: https://github.com/copper-leaf/ballast/issues/7
[#10]: https://github.com/copper-leaf/ballast/issues/10
[a]: https://github.com/Tinder/StateMachine
