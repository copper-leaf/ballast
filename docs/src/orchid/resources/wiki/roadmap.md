---
---

# Ballast Roadmap

The core Ballast functionality is now stable since 1.0.0 and will be maintained through Semantic Versioning and enforced
with [Kotlin Binary Compatibility Validator][b]. The API of additional features will also be maintained, but there's 
still a lot of functionality that could be added.

The following are some ideas of what could be done with Ballast and its Interceptor API, though nothing on this list is 
an actual hard commitment to being developed. If you see an idea on this like you want, by all means, go build it
yourself! I will gladly accept any contribution as a PR, or if you would rather develop the feature in a separate repo
I will happily link to that repo from the Ballast documentation.

- [Finite State Machine DSL][#7]: Ballast is basically an FSM already, and it would be nice to build a dedicated DSL
  like [Tinder/StateMachine][a] on top of Ballast so it could be used on any Kotlin target
- [Refactor Repository module][#31]: The Repository Module has some good ideas about how to use the MVI for managing the 
  repository an other layers of your application, extending the MVI pattern beyond just the UI. But the current 
  implementation needs to be refined and streamlined.
- [Refactor test module][#31]: The Test module currently uses some pretty hacky implementation details to run the tests
  and gather the results correctly, but it should be possible to collect all the data needed and handle all interactions
  just with an Interceptor, which would be much easier to maintain. 
- [Update IntelliJ Plugin UI][#12]: Currently, the debugger panel in the IntelliJ Plugin uses Material Compose UI, whose 
  UI elements are too big and just look out of place in the IDE. It would be nice to rewrite the UI using something like 
  the [Compose Jetbrains Theme][c] (and maybe add some additional features in the process).

[#7]: https://github.com/copper-leaf/ballast/issues/7
[a]: https://github.com/Tinder/StateMachine
[b]: https://github.com/Kotlin/binary-compatibility-validator
[#31]: https://github.com/copper-leaf/ballast/issues/31
[#12]: https://github.com/copper-leaf/ballast/issues/12
[#6]: https://github.com/copper-leaf/ballast/issues/6
[c]: https://github.com/DevSrSouza/compose-jetbrains-theme
