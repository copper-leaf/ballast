---
---

### How do I do nested sub-graphs?

"Nested sub-graphs" in terms of pure navigation really aren't necessary, and is something of an anti-pattern that has
become popularized by the Androidx Navigation library. There's not really a good reason to group a bunch of destinations
and set up a hierarchy of routers/navControllers, which just adds unnecessary complexity without much benefit.

One useful feature of Android's Nested NavGraphs, however, is the ability to scope a ViewModel to the sub-graph rather
than to an individual screen. This allows you to carry information between multiple screens in a "flow" without needing
to serialize it all in the Repository layer and manage when it should be reused/cleared. If the ViewModel data is
ephemeral and the ViewModel is discarded once the sub-graph is exited, then scoped ViewModels automatically clean up 
that data after use.

Right now, this feature is not supported in Ballast, and I'm still exploring possible options for handling this kind of
"sub-graph" scoping. You can use `RouteAnnotations` to define the bounds of a "sub-graph" and handle the purely
navigational use-case, but it's left up to you to determine how to manage the scope of ViewModels within those graphs.
Scoping ViewModels to the backstack (or anything else, really) is probably more appropriately handled by your DI
library's scope functionality, anyway, rather than Ballast itself.

### How do I save/restore the backstack?

This is intentionally left out of this library, because I did not want to tie it directly to any serialization mechanism
or library. But this is easy enough to achieve on your own, all you need to do is persist the original destination URLs
and then restore them within an Input. This example shows how it might be done (if you are using `RouteAnnotations`,
you'll want to (de)serialize those as well).

```kotlin
fun saveBackstack(router: Router<AppScrees>) {
    val backstackUrls: List<String> = router.observeStates().backstack.map { it.originalDestinationUrl }
    saveUrlsToSavedState(backstackUrls)
}

fun restoreBackstack(router: Router<AppScrees>) {
    val backstackUrls: List<String> = getUrlsFromSavedState()
    router.trySend(RestoreBackstack(backstackUrls))
}

public data class RestoreBackstack<T : Route>(
    val destinations: List<String>,
) : Inputs<T>() {
    override fun BackstackNavigator<T>.navigate() {
        val restoredBackstack = destinations.map { matchDestination(it) }
        updateBackstack { restoredBackstack }
    }

    override fun toString(): String {
        return "RestoreBackstack($destinations)"
    }
}
```

### Why does this library force Ballast MVI state management?

The technical implementation of this library actually does allow one to use a different mechanism for managing state. 
All Navigation classes and features are completely separate from any core Ballast APIs, and it's entirely possible to 
lift the Navigation code and place it into another State Management library.

But if that is true, why is it coupled to the Ballast library?

The main reason is that Routing needs some kind of state management solution in order to work properly. Things could end
up very poorly if your app attempts to make multiple navigation attempts quickly and the Router state gets corrupted, 
and you users will be very unhappy with their experience using that app. The Router state needs to be protected from
unwanted changes and ensure things are being processed safely, so the options for building the routing library then
become:

1) Keep the Navigation library completely separate from any State Management library
2) Couple it to a specific State Management library
3) Provide adapters to all the popular State Management libraries, so developers can choose which one they want to use

If I went with option 1), then the reality is that I would need to build some minimal state-management system specific 
to that library in order to allow its usage without pulling in a larger State Management library. It cannot simply exist
without state management, so it would need to be shipped with a minimal (and probably poorly-implemented solution) 
instead to avoid any external dependencies. This would then mean it is lacking in features one might expect (like 
logging, or browser-like forward/back buttons), or else have those features hardcoded into that minimal system to 
support those core use-cases that are beyond the base Navigation system. This minimal solution is simply not going to be 
a robust, extensible platform for state management that one would find in a dedicated State Management library like 
Ballast. And having built Ballast already, if I were to build a State Management solution just to ship with the 
navigation library, then I would basically just create Ballast again for it. Ballast is a pretty lightweight library, so
it just makes more sense to couple this navigation library to Ballast.

And as for the question of why not provide adapters to other libraries, the answer is that this is a maintenance burden
that I do not want to support. I do not use any other State Management libraries, myself, so I am not the best person to
maintain an adapter using Ballast Navigation with those other libraries. I also intentionally crafted this library to 
work well with the other Ballast modules, providing that additional functionality that I do not want to hardcode into 
the navigation system itself. Using Ballast Navigation with those other solutions loses those features, and would 
require a lot of extra documentation and testing to ensure everything's working properly with each library. It also
makes it more difficult for users to get started, as they could easily be overwhelmed at the thought of choosing a State
Management library that they may never interact with outside of Navigation. If I keep this Navigation library coupled to
Ballast, it's easy enough for users to get started without needing to know any of the intricacies of State Management or 
specific libraries, they can just use the snippets in the documentation and focus on the Navigation library itself, 
trusting that it is tested and known to work as they expect.

If you would like to use Ballast Navigation without the core Ballast State Management library, you should be able to 
exclude the `ballast-core` dependency from Gradle and wire it up to your own state management solution, as long as you 
do not reference anything from the `com.copperleaf.ballast.navigation.vm` package. While this is not an 
officially-supported way to use this library and I do not intend to keep any documentation for this use-case, I do 
intend to keep the Navigation APIs free from any core Ballast APIs, so please let me know if something does not work if 
you try this. At a high-level, [this snippet](https://kotlinlang.slack.com/archives/C03GTEJ9Y3E/p1669248216885769?thread_ts=1669053916.840399&cid=C03GTEJ9Y3E) 
posted to the Ballast Slack channel might help you get started.

### How do I do "up" navigation?

Most UI platforms have a distinction between "backward" and "upward" navigation. In a nutshell, "backward" navigation 
refers to going back to where you just came from, popping an entry off the backstack. "Upward" navigation means 
navigating to a specific Route that is considered the "parent" of the current destination. In terms of URLs, if you were
previously at `/users/me` and navigated to your last post `/post/1234` backward navigation (Android's hardware back 
button/gesture) brings you to `/users/me`, while upward navigation (the arrow in the toolbar) brings you to `/posts`. 
Put in another way, a "backward" navigation is dynamic and determined by the history of screens you've already visited. 
Upward navigation is static, navigating to a predefined destination. In most apps, the flow of navigation through the
application should match the route hierarchy, so a "back" and "up" action should do the same thing, but deep-links could
cause them to behave differently.

Ballast Navigation does not explicitly handle the use-case of "upward" navigation. Because the upward navigation is 
statically determined, one would have to explicitly describe the hierarchical structure of your routes if you wanted to
have a single `RouterContract.Inputs.NavigateUp()` action, which not only becomes cumbersome, but may not be entirely 
possible within the Kotlin type system (for example, with recursive routes or cycles in the graph). It also becomes a 
huge maintenance burden with the introduction of graph algorithms into the Navigation library, and something that is 
easy to mess up or get wrong for the end user.

But why do we need an `RouterContract.Inputs.NavigateUp()` action at all? The main idea is to navigate from one screen 
to its parent screen, and with a statically-defined graph, that parent route would also be statically determined. So 
rather than including a `NavigateUp` action and massively complicating this library, it's recommended to instead just 
set the action on the toolbar back button to `RouterContract.Inputs.ReplaceTopDestination()` with the intended parent
route. This actually makes it easier to understand your application's navigational flows, while keeping the core Routing
mechanism simple and easy to work with.

[1]: {{ 'Ballast Debugger' | link }}
[2]: {{ 'Ballast Undo' | link }}
[3]: {{ 'Ballast Sync' | link }}
[4]: {{ 'Ballast Firebase' | link }}
[5]: {{ 'Usage Guide' | link }}
[6]: {{ 'Navigation' | link }}
[7]: https://ktor.io/docs/routing-in-ktor.html#match_url
[8]: https://github.com/rjrjr/compose-backstack
[9]: https://developer.android.com/guide/navigation/navigation-pass-data
[10]: https://developer.mozilla.org/en-US/docs/Web/API/History_API
[11]: https://github.com/gmazzo/gradle-buildconfig-plugin
[12]: https://github.com/hfhbd/routing-compose#development-usage
