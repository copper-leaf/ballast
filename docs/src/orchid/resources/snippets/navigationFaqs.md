---
---

### Why make yet another routing library?

The first reason, and why most people create new libraries, is that I was not happy with any of the existing solutions
out there. It's my opinion that Android's official navigation patterns (both the old, manual navigation, and the newer
Androidx Navigation library) encourage patterns in navigation that tend to lead to bad application architecture. And 
unfortunately, most of the recent routing libraries I've tried seem to be copying that similar navigation patterns, 
bringing Android's anti-patterns with them into the KMPP and Compose world. Compose and MVI as an ecosystem work because
they're not trying to copy old UIs patterns, so why are we still thinking that the old style of Navigation works?

Most notably, Android's navigation system encourages a pattern of navigating to one screen, and then to another, loading
specific data on those screens as you go. Whether this is done with navigation from Activity-to-Activity, 
Fragment-to-Fragment, or by defining a specific navigation order through a declarative NavGraph explicitly linking 
destinations to one another, this style of navigation usually leads to data being loaded on a specific screen vs being 
loaded when requested, regardless of the screen requesting it. This becomes problematic when trying to implement 
deep-links, when one needs to add explicit handling of the deep-link case to load the data that would have been loaded 
on an earlier screen with the "happy path" navigation. Instead, I believe the web's pattern of every screen being 
defined by a URL and the user may jump directly to any given screen encourages a better pattern where you cannot assume 
any given sequence of screens was visited, and thus you must push the loading of data out of the UI and into the
Repository layer, where it belongs.

The second reason that I created this library is that I realized routing is really just an exercise in state management, 
and Ballast is already very good at that. Routing libraries typically build up a subsystem for managing updates to the 
state, and then build their routing logic within that, but because they're fundamentally _routing_ libraries and not 
_state management_ libraries, the actual state management aspects of them are lacking.

But Ballast is already proven to be a stable, robust, and predicable state management library, and it was relatively
simple to add navigation on top of what already exists here. And in the process, Ballast Navigation gains all the
features of the other Ballast extension libraries for free (like logging, debugging, or undo/redo), both current and
future, which would otherwise either be hardcoded in hacky ways into those other libraries, or else completely absent.

### Is this library type-safe?

It depends on what you mean by type-safe. If, by that, you mean that routing is done with data classes that are just
passed around, then no, this library is not type-safe. It works by parsing a URL to extract data from the path and query
parameters, and those values are ultimately passed around as Strings, not as strongly-typed objects.

But if by type-safe you mean that when loading a route, you can easily ensure that the parameters exist and are of a
certain type, then yes, this library does support that. Route matching is strict and you manually define which
parameters must be present, and it offers a set of delegate functions to make it easy to extract those parameters in a
type-safe manner, preventing you to navigating to a route if the value is of an incorrect type. This style of routing is
not checked at compile time, unlike passing around a data class, but it actually has some other advantages that the
data-class argument-passing lacks:

- By forcing you to represent the data passed between routes as a URL, it encourages the best-practice of only passing
  the minimal amount of data needed for the new route to load the full objects it needs. Quoting from the documentation
  of [Androidx Navigation][9], _"In general, you should strongly prefer passing only the minimal amount of data between
  destinations. For example, you should pass a key to retrieve an object rather than passing the object itself...If you
  need to pass large amounts of data, consider using a ViewModel as described in 'Share data between fragments'."_
- You get deep-linking for free, since effectively _every_ navigation request is a deep-link. If you have to pass 
  configuration/argument objects, you would have to manually parse a deep-link URL to that object before attempting to 
  navigate with it, which can cause problems if your URL-parsing logic differs from the rest of your application's 
  navigation logic.
- KSP and Code Generation, or type-safe wrapper functions, can be easily added on top of this library, while it's more 
  difficult to take a library built with strong type-safety/code generation in mind and use it in any other way. This 
  eases the burden of evaluation or incremental adoption. For example, generating type-safe Directions functions and 
  arguments delegates could be done fairly easily, and the core routing APIs were intentionally designed to allow that 
  possibility, though it is not on the current roadmap for this library. This would be a very welcome addition from the 
  community, if someone wanted to create this as a KSP plugin!

### Does this library integrate with Compose?

Yes! Everything you need to integrate Ballast Navigation into Compose is provided in the core artifact, without any need
for a special Compose integration library. Ballast Navigation ultimately just manages a backstack of URLs and emits it 
to the UI as a `StateFlow`, which can be easily collected from Compose. Anything else that you would typically want from
a "Compose integration" is almost certainly too specific to your use-case to be included within the core Ballast 
Navigation library, but is easy enough for you to implement yourself.

But when people typically ask this question, what they really are asking is, "does it live entirely within Compose code, 
and give me automatic transition animations and stuff like that". And the answer to this question is no, Ballast 
Navigation is intentionally kept outside the UI. A community-designed library to connect Ballast Navigation to Compose 
for things like Animations would be a very welcome addition, however!

For now, you can achieve basic transition animations with existing Compose UI APIs like `AnimatedContent`. Or if someone
wanted to help bring [rjrjr/compose-backstack][8] up-to-date with the latest Compose version and make it work with
Desktop, that would be the perfect companion library to Ballast Navigation!

### How do I sync destinations with the browser address bar?

When using Ballast Navigation in the browser, you may wish to show the current destination URL in the browser's address
bar to help the user understand the structure of your application, as well as allowing them to edit the URL to jump to
a specific screen, or save it as a bookmark.

This is included as built-in functionality, for synchronizing the router state with the browser's address bar in both
directions: applying router state to the address bar, and passing changes made by the user back into the router. It will
also take care of reading the current URL when the page first loads, and navigating directly to that route.

All that's needed to support this functionality is to add an Interceptor to the Router during creation. Both hash-based
routing and the [History API][10] are supported.

#### Browser Hash

Hash-based routing is the "older" mechanism for routing in a Single Page Application (SPA), though it should not be
considered obselete. In particular, one would have to set up server-side redirects to make the History API work, which
may not be feasible, in which case Hash-based routing is the only option left.

Hash-based routing can be added with the `BrowserHashNavigationInterceptor`, or with the `withBrowserHashRouter` helper
function.

```kotlin
class RouterViewModel(
    viewModelCoroutineScope: CoroutineScope
) : BasicRouter<AppScreens>(
    config = BallastViewModelConfiguration.Builder()
        .withBrowserHashRouter(RoutingTable.fromEnum(AppScreens.values()), AppScreens.Home)
        .build(),
    eventHandler = eventHandler { },
    coroutineScope = viewModelCoroutineScope,
)
```

#### Browser History

Hash-based routing is done with the `#` portion of the URL, and isn't as user-friendly to read and share as with just
a normal URL path. The [Browser History API][10] allows websites to edit the entire URL shown in the address bar
and navigate forward and backward through the screens of your SPA with the browser's native buttons, so users wouldn't
even know that you'ure doing front-end routing.

The caveat is that using the history API requires your hosting server to redirect all URLs to the SPA's main page. There
are plenty of tutorials online for configuring your server to do this, so I will not cover these details here.

Routing with the History API can be added with the `BrowserHistoryNavigationInterceptor`, or with the
`withBrowserHistoryRouter` helper function. Unlike the Hash interceptor, the History interceptor needs to know which
portion of the URL path is just the page itself, and which is used for routing within the application, so you must pass
the base path for this page into the interceptor.

```kotlin
class RouterViewModel(
    viewModelCoroutineScope: CoroutineScope
) : BasicRouter<AppScreens>(
    config = BallastViewModelConfiguration.Builder()
        .withBrowserHistoryRouter(RoutingTable.fromEnum(AppScreens.values()), basePath = "/app", initialRoute = AppScreens.Home)
        .build(),
    eventHandler = eventHandler { },
    coroutineScope = viewModelCoroutineScope,
)
```

I would recommend using the `BrowserHashNavigationInterceptor` when developing locally and switch it out for
`BrowserHistoryNavigationInterceptor` when deploying to production, so you don't have to mess with your Webpack dev
server configuration. There are several ways to determine if your running in production, such as checking the value of
`window.location.host`, setting a property as a hidden element in the page's HTML, or using something like
[Gradle BuildConfig plugin][11] to inject a value from the build pipeline into the Kotlin code. But if you do want to
use the `BrowserHistoryNavigationInterceptor` in development, [routing-compose][12] has instructions for getting your
environment set up.

### How does this library handle transition animations?

It doesn't. Ballast Navigation just manages the backstack, but you can apply transition animations yourself when
handling route changes. Ballast Navigation intentionally keeps itself separate from the UI to allow maximum flexibility
and avoid bloat in its API.

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
