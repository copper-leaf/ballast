# Ballast Navigation

## Overview

Ballast Navigation is a Kotlin multiplatform URL-based routing library, built on top of the rock-solid Ballast state
management library. It is framework-agnostic and can be easily integrated into Compose, Android, or any other
application where you need to handle routing or navigation. It works purely at runtime with no reflection, no code
generation, and no magic. Just simple, predictable state management, like a browser's address bar anywhere you need it.

Ballast Navigation essentially just provides a way to manage a backstack of URLs, and match those URLs to registered
routes using a pattern syntax similar to Ktor's router. It manages backstack updates safely and predictably, and since
it is built with Ballast at the core, you can extend your routing functionality with features like:

- Time-travel debugging and inspecting the backstack with the [Ballast Debugger][1]
- Adding browser-like forward/backward navigation buttons with [Ballast Undo][2]
- Synchronizing router state across components or devices with [Ballast Sync][3]
- Tracking page views with [Ballast Analytics][4]

## Supported Platforms

| Platform | Supported |
|----------|-----------|
| JVM      | ✅         |
| Android  | ✅         |
| iOS      | ✅         |
| JS       | ✅         |
| WASM JS  | ✅         |

## See Also

## Usage

Ballast Navigation can be used as your application's main router, or as a sub-router for tabbed views or similar UI
patterns, and there's no real difference between the two. This usage guide will walk you through the basics needed to
start handling navigation with Ballast, which can be applied to any navigational pattern you need. It's helpful to have
an understanding of the Ballast MVI model first, which you can find in the main [Ballast Usage Guide][5], but this is
not strictly necessary.

First, let's define some terms, which will make the rest of the documentation easier to understand:

- **Destination**: A URL that has been sent to the router and lives in the Backstack. A Destination is either matched to
  a route, or set as a "mismatch" (like a 404 page in a website)
- **Route**: Destination URLs are matched to Routes, which may include dynamic path or query parameters extracted
  from the destination URL.
- **Routing Table**: A container which holds registered Routes, and matches destination URLs to a registered route.
- **Backstack**: A simple list of Destinations, where the last entry in the list is considered the
  "current destination". You move deeper into the application by pushing new destinations onto the end of the stack, and
  go backward by popping the last destination off the stack. The state of the backstack can only be updated by sending
  an "Input" to the Router, which requests a particular change (or set of changes) be performed which modify the stack.
- **Router**: A Ballast ViewModel that manages the backstack and protects it from unexpected changes. Changes to the
  backstack will be set as the ViewModel's State, which can be observed directly from a declarative UI, and will also be
  sent as discrete Events for handling navigation in a more imperative manner (such as controlling Android
  FragmentTransactions).

### Step 1: Define your Routes

Start by defining your routes. This is done with an enum class so that you can statically refer to all routes anywhere
in your application, since enums are effectively constant values. Enums also allow you to use an exhaustive `when` to
display UI for a given route, and also automatically registers all routes with the Routing Table without additional
boilerplate, code generation, or reflection magic. This ensures that any route you create will always be handled
properly, both in the Routing Table and in your UI.

The enum class that you use to define your Routes must implement the `Route` interface, as shown in this snippet:

```kotlin
enum class AppScreen(
    routeFormat: String,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    Home("/app/home"),
    PostList("/app/posts?sort={?}"),
    PostDetails("/app/posts/{postId}"),
    ;

    override val matcher: RouteMatcher = RouteMatcher.create(routeFormat)
}
```

The syntax for matching routes is documented in more detail [below](#route-matching).

### Step 2: Create the Router object

The Router is just a Ballast ViewModel, which can be created using any implementation class you need. You must call
`.withRouter()` on the `BallastViewModelConfiguration.Builder` and pass in your RoutingTable and the initial route,
which is created using `RoutingTable.fromEnum()`.

The Router should typically be effectively global and managed at the root of your application, since it controls the
state of all screens in your application. In other words, it lives _above_ the UI, not within it. Alternatively, you
can create routers for locally-scoped portions of the application like tabbed views, which should be managed at that
point in the application instead of globally.

Here's an example of creating a ViewModel class to be your Router. The classes typically needed for a Ballast ViewModel
are all further parameterized with the type of Route, so typealiases are available which reduce the boilerplate you need
to write. `BasicViewModel<>` becomes `BasicRouter<>`, `EventHandler<>` becomes `RouterEventHandler<>`, etc.

```kotlin
class RouterViewModel(
    viewModelCoroutineScope: CoroutineScope
) : BasicRouter<AppScreens>(
    config = BallastViewModelConfiguration.Builder()
        .withRouter(RoutingTable.fromEnum(AppScreens.values()), AppScreens.Home)
        .build(),
    eventHandler = eventHandler { },
    coroutineScope = viewModelCoroutineScope,
)
```

!!! info

    When using Ballast Navigation in the browser, you can use `.withBrowserHashRouter()` or `.withBrowserHistoryRouter()`
    instead of `.withRouter()` to synchronize the Router state with the browser's address bar. See
    [FAQs below](#how-do-i-sync-destinations-with-the-browser-address-bar) for more info on this feature.

Refer to the Usage Guide
for full documentation on creating the ViewModel for your platform's needs.

### Step 3: Handle route changes

Now that the Router is set up and ready to accept navigation requests, it's time to decide how you'll handle route
changes. There are 2 basic ways to handle route changes, as explained below:

#### Declaratively observing Backstack State

The backstack is managed as a StateFlow within a Ballast ViewModel, and you can observe that StateFlow to apply its
changes to your UI. This is typically how one would handle navigation in Compose or other Declarative UI toolkits.

When collecting the Router State, you would typically only look at the last entry of the backstack to determine the
"current route" that should be displayed in the UI. `routerState.renderCurrentDestination` is the easiest way to display
the current Route or a "Not Found" screen, but there are several other extension functions for more specific use-cases
that you may find useful. And of course, the backstack is just a list of states, so you are free to consider entries
further back in the stack, such as for showing a stack of floating windows.

```kotlin
@Composable
fun MainContent() {
    val applicationScope = rememberCoroutineScope()
    val router: Router<AppScreen> = remember(applicationScope) { RouterViewModel(applicationScope) }

    val routerState: Backstack<AppScreen> by router.observeStates().collectAsState()

    routerState.renderCurrentDestination(
        route = { appScreen: AppScreen ->
            when(appScreen) {
                // ...
            }
        },
        notFound = { },
    )
}
```

#### Imperatively reacting to Backstack changes

Other (usually older) UI toolkits typically worked with a more imperative mechanism for handling navigation between
screens. This would be the traditional Activity- or Fragment-based navigation on Android for example. Ballast Navigation
is able to work with this style of navigation by handling changes in a Ballast Event Handler to ensure they're only
handled once for each screen.

Here's an example of how this might look for a single-Activity Fragment-based navigation in Android. You'll notice that
it uses all of the same extension functions as the Declarative Compose model for finding the current screen in the
backstack, accessing route parameters, etc.

```kotlin
class BallastExamplesRouterEventHandler(
    private val activity: MainActivity,
) : RouterEventHandler<AppScreens> {

    private fun getFragment(
        route: BallastExamples,
    ): Class<out Fragment> = when (route) {
        Home -> HomeFragment::class.java
        PostList -> PostListFragment::class.java
        PostDetails -> PostDetailsFragment::class.java
    }

    override suspend fun RouterEventHandlerScope<BallastExamples>.handleEvent(
        event: RouterContract.Events<BallastExamples>
    ) = when (event) {
        is RouterContract.Events.BackstackChanged -> {
            // figure out the Fragment to navigate to, and supply the Fragment with arguments parsed from the 
            // Destination URL
            val currentDestination = event.backstack.currentDestinationOrThrow
            val fragment = getFragment(currentDestination.originalRoute)
            val args = currentDestination.toBundle()

            // perform a fragment transaction
            activity
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, args)
                .commit()

            Unit
        }

        is RouterContract.Events.BackstackEmptied -> {
            // exit the application
            activity.finish()
        }

        is RouterContract.Events.NoChange -> {
            // do nothing
        }
    }
}
```


!!! info

    If navigating with Android Fragments or Activities, use `Destination.Match.toBundle()` to capture the path and query
    parameters and pass them into the destination Fragment via its arguments. That Fragment can then convert its arguments
    back into the Ballast Navigation destination parameters with `Bundle.toDestinationParameters()` so that you can set up
    parameter delegates within the class body. For example:
  
    ```kotlin
    class PostDetailsFragment : Fragment(), Destination.ParametersProvider {
        override val parameters: Destination.Parameters by lazy { requireArguments().toDestinationParameters() }
        private val postId by stringPath()
    }
    ```

### Step 4: Navigate!

All that's left is to handle your application logic to send navigation requests to the Router! As the Router is just a
Ballast ViewModel, this is done by sending an `Input` to the Router requesting some change. There are several Inputs
available out-of-the-box, but you're free to create custom Inputs to handle more specialized navigation logic, by
extending the `RouterContract.Inputs` base class.

The available Inputs are:

- **RouterContract.Inputs.GoToDestination(destination: String)**: Push a destination URL into the backstack,
  attempting to match it against a registered Route. If the current destination was a mismatch, it will be removed, such
  that only 1 destination in the backstack would be a Mismatch, and it would always be the last entry. If the
  destinationUrl is the exact same as the current destination, then the navigation request will be ignored. This is
  typically used for the application's main router, or anywhere you want to navigate forward and back (such as with an
  Android phone's back gestures/hardware button).
- **RouterContract.Inputs.ReplaceTopDestination(destination: String)**: Pop the current destination off the backstack
  before pushing a new destination in, using the same logic as with `RouterContract.GoToDestination`. This is typically
  used for creating tabbed views or other "lateral" navigation, where the selected tab should not be affected by
  backward navigation gestures.
- **RouterContract.Inputs.GoBack()**: Pop the current destination off the backstack, returning to the destination before
  it. If there was only 1 entry in the backstack, then the `BackstackEmptied` event will be emitted to the EventHandler,
  indicating that you should handle the case, such as by exiting the application.

```Kotlin
router.trySend(
    RouterContract.Inputs.GoToDestination("/app/posts/12345")
)
```

You'll notice that the Inputs to go to a Destination all take a String URL, rather than a Route. This is intentional, as
Routes should always come from the RoutingTable registered with the Router, and not be provided externally. Instead, you
navigate to a URL, and that URL is matched to a Route where it's parameters are parsed from the URL. This makes sure
you are not putting data into the Destination URL that cannot be easily serialized, and enforces the best practice of
only sending identifiers through the navigation request, rather than full objects. It also sets you up immediately to
handle deep-links without any special logic for translating those deep link URLs into discrete configuration objects, as
would be required by other "type-safe" routing libraries.

That said, Ballast Navigation makes it easy to generate a URL for a given Route, by using the `.directions()` extension
function. You can pass path and query parameters into this function, where it will insert them into the appropriate
places within the URL and return a String URL that will be matched by that same Route.

```Kotlin
router.trySend(
    RouterContract.Inputs.GoToDestination(
        AppScreen.PostDetails
            .directions()
            .pathParameter("postId", postId.toString())
            .build()
    )
)
```

## Route Matching

The syntax used for matching Destinations to Routes is inspired by the patterns used for [Ktor Server Routing][7]. In
fact, it was designed to be an extension of that syntax, but with additional support for matching query parameters, so
any routes used by Ktor should also be compatible with Ballast Navigation.

One significant difference from the Ktor syntax, however, is that Ballast Navigation requires query parameters to be
explicitly stated in the pattern, while Ktor does not have a syntax available to specify query parameters.

### Path Format

The Path format is a sequence of path segments separated by a slash `/` character. The path must start with a slash, and
trailing slashes are ignored.

Most of the following documentation is taken directly from Ktor. If the Ktor syntax changes, you can expect that Ballast
Navigation will also be updated to match that change. Also, if you encounter a URL path format that works in Ktor but
not in Ballast Navigation, please open an issue so that this can be remedied.

The following examples taken from the Ktor documentation are also valid routes in Ballast Navigation:

- `/hello`: A path containing a single path segment.
- `/order/shipment`: A path containing several path segments.
- `/user/{login}`: A path with the login path parameter, whose value can be accessed inside the route handler.
- `/user/*`: A path with a wildcard character that matches any path segment.
- `/user/{...}`: A path with a tailcard that matches all the rest of the URL path.
- `/user/{param...}`: A path containing a path parameter with tailcard.

#### Wildcard

A wildcard (`*`) matches any path segment and can't be missing. For example, `/user/*` matches `/user/john`, but doesn't
match `/user`.

#### Tailcard

A tailcard (`{...}`) matches all the rest of the URL path, can include several path segments, and can be empty. For
example, `/user/{...}` matches `/user/john/settings` as well as `/user`.

If a Destination includes a names tailcard, its value can be accessed like
`destination.pathParamters["param"]`.

#### Path Parameter

A path parameter (`{param}`) matches a path segment and captures it as a parameter named `param`. This path segment is
mandatory, but you can make it optional by adding a question mark: {`param?`}. `:param` can be used as an alternative
syntax for `{param}`, and cannot be made optional. For example:

- `/user/{login}` matches `/user/john`, but doesn't match `/user`.
- `/user/:login` matches `/user/john`, but doesn't match `/user`.
- `/user/{login?}` matches `/user/john` as well as `/user`.

Note that optional path parameters {param?} can only be used at the end of the path. Also, optional path parameters
cannot be used with a tailcard, you must choose one or the other.

If a Destination includes a path parameter, its value can be accessed like
`destination.pathParamters["param"]`, or by using the delegate functions like
`val param: String by destination.stringPath()`, `val param: Int? by destination.optionalIntPath()`, etc.

### Query Parameter Format

The Query String format is a sequence of `key=value` pairs separated by `&`, separated from the path with `?`. Unlike
Ktor routes, Ballast Navigation requires all query parameters to be accounted for in the route format, and destinations
can be matched to different routes which have the same path but different query parameters.

The following examples are valid routes in Ballast Navigation:

- `/hello?name=Ballast`: A query parameter where both the key and value are statically defined.
- `/greeting?name={!}`: Show a greeting, where a single name must be provided
- `/posts?sort={?}`: Display a list of posts, and optionally provide a value for how to sort the list
- `/email/compose?recipients={[!]}`: Compose an email to send to a list of recipients. You must have at least 1 recipient,
  but may have more than 1. The destination URL collects multiple query parameters at the same key to the same list of
  values, so even though only 1 key for `recipients` is present in this format, multiple `recipient=email` values may be
  present in the destination.
- `/template/render?template={!}&emailPreviewTo={[?]}&{...}`: Render a template as HTML. The template filename must be
  provided, and you may optionally pass a list of names to send a preview to. Any additional query parameters may be
  passed through, which would be made available to the template language.

#### Static Query

Static query parameters may be set to only match parameters with a specific value, using the standard URL query string
syntax of `?key1=value1&key2=value2`. If you require a key to have a hardcoded list of values, you must use a list value
rather than multiple pairs with the same key, like `key=[value1,value2]`.

#### Query Parameter

Query parameters at a given key are defined with a syntax like `key={!}`. The value inside the braces determines how
many values are allowed at that key:

` /route?one={!} `: require exactly 1 value
` /route?one={[!]}`: require 1 or more values
` /route?one={?} `: allow 0 or 1 value
` /route?one={[?]}`: allow 0 or more values

If a Destination includes query parameters, they ma be accessed like
`destination.queryParamters["param"]`, or by using the delegate functions like
`val param: String by destination.stringQuery()`, `val param: Int? by destination.optionalIntQuery()`, etc.

#### Remaining Query

The remaining query is not defined as a key-value pair, but instead as `{...}`. It is effectively a Tailcard for query
parameters, where anything that was not matched from previous query parameters will be passed through. The remaining
query parameters may be empty.

If a Destination includes query parameters, they may be accessed like
`destination.queryParamters["param"]`, or by using the delegate functions like
`val param: String by destination.stringQuery()`, `val param: Int? by destination.optionalIntQuery()`, etc.

### Route Weights

Routes in Ballast Navigation are weighted such that more "specific" formats will be matched before those with fewer
matching criteria. When a Route is parsed with `RouteMatcher.create(routeFormat)`, it will compute a weight for that
route (which is just an arbitrary Double), and the routes passed to the RoutingTable will be sorted by weight and
searched in that order for a match. The specific values defined as the weight for a route is not intended to be used for
anything meaningful other than relative ordering between routes, and the implementation for computing a route's weight
is subject to change.

The weighting algorithm is defined such that, by default, routes with more path segments or query parameters should be
selected over those with fewer, and statically defined values are more specific than parameters or wildcards.
Additionally, for routes with the same number of path segments and/or query parameters, paths segments are given a
higher weight. The more "specific" a route is, or the more path segments it has, the more likely it is to be matched
over less specific ones or ones with query parameters, though this is not necessarily a strict guarantee.

For example, `/one/{two?}?three={!}` and `/one?two={?}&three={!}` will both match the destination `/one?three=four`,
but since the first route has an additional path segment it will be selected as the route over the second, even though
they both had 3 total "url pieces". Likewise, the routes `/one/two` and `/one/{two}` will both match a URL of `/one/two`,
but the first route will be selected since all path segments are static, while the second route has dynamic parameters.

In some cases, you may have 2 routes with similar "specificity", where the default weighting algorithm does not select
the route you expect. In this case, you can set a hardcoded weight for those routes rather than letting them be computed
automatically. This can be set in the call to `RouteMatcher.create(routeFormat)` within your Route enum class, by
overriding the `computeWeight` lambda. As you should not rely on any specific values for the computed weights, you
should manually define the weights for all affected routes to be higher than anything that could be computed. This is
most easily done by using weights on the order of `Double.MAX_VALUE` (`Double.MAX_VALUE - 1`, `Double.MAX_VALUE - 2`,
etc.) to ensure you do not assign a weight lower than would have been created algorithmically, making it harder to match
those routes.

```kotlin
enum class AppScreen(
    routeFormat: String,
    hardcodedWeight: Double? = null,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    Home("/app/home"),
    PostList("/app/posts?sort={?}"),
    PostDetails("/app/posts/{postId}"),
    SimilarWithPath("/one/{two?}?three={!}", Double.MAX_VALUE - 2),
    SimilarWithQuery("/one?two={?}&three={!}", Double.MAX_VALUE - 1), // this route will be selected over SimilarWithPath
    ;

    override val matcher: RouteMatcher = if(hardcodedWeight != null) {
        RouteMatcher.create(routeFormat) { path, query -> hardcodedWeight }
    } else {
        RouteMatcher.create(routeFormat)
    }
}
```

### Route Annotations

Route Annotations are a way to attach metadata to a Destination, either as part of the Route, or directly through the
navigation request. This metadata is never used for matching a Destination URL to a Route, but instead can be used to
help change how the Route is displayed (in a floating window vs. fullscreen, for example), or to help you navigate
through the backstack (popping off all destinations with a given tag). Internally, it is already in use to aid in
syncing the URL with the browser address bar.

!!! warning

    This feature hasn't been thoroughly tested yet. Use it at your own risk, it may be changed or replaced in the future.

!!! danger

    Do not use Route Annotations for passing data between screens. Always pass information through path or query parameters,
    or lift larger objects into a ViewModel or your Repository layer that is shared by the originating and destination
    screens.

A Route Annotation is a class that implements `RouteAnnotation`, which is simply a marker interface. This is intended to
require Route Annotations to be special classes used only for the purpose of metadata, and prevent you from passing
arbitrary data through the Annotation. You are free to create your own RouteAnnotations, but you should always treat
these classes as through they were like regular Kotlin `annotation classes`, containing only simple, constant,
serializable values. Additionally, there are a couple Route Annotations provided out-of-the-box for the use-cases
mentioned at the start of this section:

- `Tag("tag name")`: Set a String tag to this route for aid in backstack navigation. For example, you can use tags to
  define the routes in a navigation sub-graph, and then exit the entire flow by popping all destinations with that
  flow's tag.
- `Floating`: Request the destination to be displayed in a Floating window. It's up to you to actually display the
  destination's content like this.

Route Annotations may be set on the Route, which will get added to every Destination matched to that Route:

```kotlin
enum class AppScreen(
    routeFormat: String,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    Home("/app/home"),
    PostList("/app/posts?sort={?}"),
    PostDetails("/app/posts/{postId}", annotations = setOf(Floating)), // request this route to be displayed in a floating window
    ;

    override val matcher: RouteMatcher = RouteMatcher.create(routeFormat)
}
```

You can also provide Route Annotations directly to the navigation request:

```Kotlin
router.trySend(
    RouterContract.Inputs.GoToDestination(
        destination = "/app/posts/12345",
        extraAnnotations = setOf(Floating), // normally this destination is displayed fullscreen, but this time only display it in a floating window
    )
)
```

All matched destinations will contain a Set of Route Annotations, which can be when displaying the backstack content or
during handling a navigation request in the `BackstackNavigator`. If you are doing anything where you must save and
restore the Backstack, these `RouteAnnotations` should generally be saved and restored along with the destination URLs.

## FAQs

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

Automatic state restoration is intentionally left out of this library, because I did not want to tie it directly to any
serialization mechanism or library. But this is easy enough to achieve on your own, all you need to do is persist the
original destination URLs and then restore them within an Input. This example shows how it might be done (if you are
using `RouteAnnotations`, you'll want to (de)serialize those as well).

```kotlin
fun saveBackstack(router: Router<AppScreen>) {
    val backstackUrls: List<String> = router.observeStates().value.map { it.originalDestinationUrl }
    saveUrlsToSavedState(backstackUrls)
}

fun restoreBackstack(router: Router<AppScreen>) {
    val backstackUrls: List<String> = getUrlsFromSavedState()
    router.trySend(RouterContract.Inputs.RestoreBackstack(backstackUrls))
}
```

Automatically saving/restoring the state can be done with the help of the [Ballast Saved State module][13], by creating an
adapter like this:

```kotlin
/**
 * Automatically save and restore the state of the Router with any route changes. Do not pass an initial route to the
 * BallastViewModelConfiguration.Builder.withRouter()` when using this adapter, as it will handle setting the initial
 * route instead, and may conflict with the initial route set through that function.
 *
 * The actual serialization and persistence of the backstack is delegated through [prefs].
 *
 * If you are also using the Ballast Undo/Redo module for forward/backward navigation, set [preserveDiscreteStates] to
 * true so the backstack is restored through individual [RouterContract.Inputs.GoToDestination] Inputs to capture each
 * intermediate state. If not, it can be set to false so that a single [RouterContract.Inputs.RestoreBackstack] is used
 * instead.
 */
public class RouterSavedStateAdapter<T : Route>(
    private val routingTable: RoutingTable<T>,
    private val initialRoute: T?,
    private val prefs: Prefs,
    private val preserveDiscreteStates: Boolean = false,
) : SavedStateAdapter<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>> {

    public interface Prefs {
        var backstackUrls: List<String>
    }

    override suspend fun SaveStateScope<
            RouterContract.Inputs<T>,
            RouterContract.Events<T>,
            RouterContract.State<T>>.save() {
        saveAll { backstack ->
            prefs.backstackUrls = backstack.map { it.originalDestinationUrl }
        }
    }

    override suspend fun RestoreStateScope<
            RouterContract.Inputs<T>,
            RouterContract.Events<T>,
            RouterContract.State<T>
            >.restore(): RouterContract.State<T> {
        val savedBackstack = prefs.backstackUrls
        if(savedBackstack.isEmpty()) {
            initialRoute?.let { initialRoute ->
                check(initialRoute.isStatic()) {
                    "For a Route to be used as a Start Destination, it must be fully static. All path segments and " +
                            "declared query parameters must either be static or optional."
                }
                postInput(
                    RouterContract.Inputs.GoToDestination(initialRoute.directions().build())
                )
            }
        } else if(preserveDiscreteStates) {
            savedBackstack.forEach { destinationUrl ->
                postInput(
                    RouterContract.Inputs.GoToDestination(destinationUrl)
                )
            }
        } else {
            postInput(
                RouterContract.Inputs.RestoreBackstack(savedBackstack)
            )
        }

        return RouterContract.State(routingTable = routingTable)
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

## Full Code Snippet

The following snippet is a complete example of using Ballast for routing in a Compose application. You can
copy-and-paste it directly to your project to get started immediately, or see the [Navigation example][6] and browse its
sources to see a more production-quality example implementation. The example repos also show examples of Ballast
Navigation in [Compose Web][14], [Compose Desktop][15], and [Fragment-based Android][16] applications. The Android
example also shows how one might use the `Floating` `RouteAnnotation` to display and given Route's content in a Dialog
rather than fullscreen.

```kotlin
// Define your routes
enum class AppScreen(
    routeFormat: String,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    Home("/app/home"),
    PostList("/app/posts?sort={?}"),
    PostDetails("/app/posts/{postId}"),
    ;

    override val matcher: RouteMatcher = RouteMatcher.create(routeFormat)
}

@Composable
fun MainContent() {
    val applicationScope = rememberCoroutineScope()

    // Set up the Router, which is just a normal Ballast ViewModel
    val router: Router<AppScreen> = remember(applicationScope) {
        BasicRouter(
            coroutineScope = applicationScope,
            config = BallastViewModelConfiguration.Builder()
                .apply {
                    // log all Router activity to inspect the backstack changes
                    this += LoggingInterceptor()
                    logger = ::PrintlnLogger

                    // You may add any other Ballast Interceptors here as well, to extend the router functionality
                }
                .withRouter(RoutingTable.fromEnum(AppScreen.values()), initialRoute = AppScreen.Home)
                .build(),
            eventHandler = eventHandler {
                if (it is RouterContract.Events.BackstackEmptied) {
                    exitProcess(0)
                }
            },
        )
    }

    // collect the Router's StateFlow as a Compose State
    val routerState: Backstack<AppScreen> by router.observeStates().collectAsState()

    routerState.renderCurrentDestination(
        route = { appScreen ->
            // the last entry in the backstack was matched to a route. We will switch on which route was matched,
            // and pull path and query parameters from the destination
            when (appScreen) {
                AppScreen.Home -> {
                    HomeScreen()
                }

                AppScreen.PostList -> {
                    val sort: String? by optionalStringQuery()
                    PostListScreen(
                        sort = sort,
                        onPostSelected = { postId: Long ->
                            // The user selected a post within the PostListScreen. Generate a URL which will match
                            // to the PostDetails route, by using its directions to ensure the right parameters are
                            // provided in the URL
                            router.trySend(
                                RouterContract.Inputs.GoToDestination(
                                    AppScreen.PostDetails
                                        .directions()
                                        .pathParameter("postId", postId.toString())
                                        .build()
                                )
                            )
                        },
                    )
                }

                AppScreen.PostDetails -> {
                    val postId: Long by longPath()
                    PostDetailsScreen(
                        postId = postId,
                        onBackClicked = {
                            // The user clicked the back button, notify the router to pop the latest destination off
                            // the backstack
                            router.trySend(
                                RouterContract.Inputs.GoBack()
                            )
                        },
                    )
                }
            }
        },
        notFound = {
            // the last entry in the backstack could not be matched to a route
            NotFoundScreen(mismatchedUrl = it)
        },
    )
}

@Composable
fun HomeScreen() {
    // omitted for brevity
}

@Composable
fun PostListScreen(sort: String?, onPostSelected: (Long) -> Unit) {
    // omitted for brevity
}

@Composable
fun PostDetailsScreen(postId: Long, onBackClicked: () -> Unit) {
    // omitted for brevity
}

@Composable
fun NotFoundScreen(mismatchedUrl: String) {
    // omitted for brevity
}
```

## Installation

```kotlin
repositories {
    mavenCentral()
}

// for plain JVM or Android projects
dependencies {
    implementation("io.github.copper-leaf:ballast-navigation:{{ballastVersion}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-navigation:{{ballastVersion}}")
            }
        }
    }
}
```


[1]: ./../ballast-debugger-client/README.md
[2]: ./../ballast-undo/README.md
[3]: ./../ballast-sync/README.md
[4]: ./../ballast-analytics/README.md
[5]: ./
[6]: ./../ballast-navigation/README.md
[7]: https://ktor.io/docs/routing-in-ktor.html#match_url
[8]: https://github.com/rjrjr/compose-backstack
[9]: https://developer.android.com/guide/navigation/navigation-pass-data
[10]: https://developer.mozilla.org/en-US/docs/Web/API/History_API
[11]: https://github.com/gmazzo/gradle-buildconfig-plugin
[12]: https://github.com/hfhbd/routing-compose#development-usage
[13]: ./../ballast-saved-state/README.md
[14]: https://github.com/copper-leaf/ballast/tree/main/examples/web
[15]: https://github.com/copper-leaf/ballast/tree/main/examples/desktop
[16]: https://github.com/copper-leaf/ballast/tree/main/examples/android
