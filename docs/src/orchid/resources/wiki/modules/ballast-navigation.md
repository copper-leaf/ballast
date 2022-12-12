---
---

# {{ page.title }}

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
- Tracking page views with [Ballast Firebase Analytics][4]

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

{% alert 'info' :: compileAs('md') %}
**Info**

When using Ballast Navigation in the browser, you can use `.withBrowserHashRouter()` or `.withBrowserHistoryRouter()`
instead of `.withRouter()` to synchronize the Router state with the browser's address bar. See
[FAQs below](#how-do-i-sync-destinations-with-the-browser-address-bar) for more info on this feature.
{% endalert %}

Refer to the {{ anchor(itemId = 'Usage Guide', pageAnchorId = 'connect-to-the-platform-ui', title = 'Usage Guide') }}
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
    val router: Router<AppScreen> = remember(applicationScope) { RouterViewModel(applicationScoe) }

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


{% alert 'info' :: compileAs('md') %}
**Info**

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
{% endalert %}

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

{% alert 'warning' :: compileAs('md') %}
**Warning**

This feature hasn't been thoroughly tested yet. Use it at your own risk, it may be changed or replaced in the future.
{% endalert %}

{% alert 'danger' :: compileAs('md') %}
**Danger**

Do not use Route Annotations for passing data between screens. Always pass information through path or query parameters,
or lift larger objects into a ViewModel or your Repository layer that is shared by the originating and destination
screens.
{% endalert %}

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
    PostDetails("/app/posts/{postId}", annotations = listOf(Floating())), // request this route to be displayed in a floating window
    ;

    override val matcher: RouteMatcher = RouteMatcher.create(routeFormat)
}
```

You can also provide Route Annotations directly to the navigation request:

```Kotlin
router.trySend(
    RouterContract.Inputs.GoToDestination(
        destination = "/app/posts/12345",
        extraAnnotations = setOf(Floating()), // normally this destination is displayed fullscreen, but this time only display it in a floating window
    )
)
```

All matched destinations will contain a Set of Route Annotations, which can be when displaying the backstack content or
during handling a navigation request in the `BackstackNavigator`. If you are doing anything where you must save and
restore the Backstack, these `RouteAnnotations` should generally be saved and restored along with the destination URLs.

## FAQs

{% snippet 'navigationFaqs' %}

### More FAQs

See more FAQs [here][13]

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
    implementation("io.github.copper-leaf:ballast-navigation:{{site.version}}")
}

// for multiplatform projects
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:ballast-navigation:{{site.version}}")
            }
        }
    }
}
```

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
[13]: {{ 'Ballast Navigation FAQs' | link }}
[14]: https://github.com/copper-leaf/ballast/tree/main/examples/web
[15]: https://github.com/copper-leaf/ballast/tree/main/examples/desktop
[16]: https://github.com/copper-leaf/ballast/tree/main/examples/android
