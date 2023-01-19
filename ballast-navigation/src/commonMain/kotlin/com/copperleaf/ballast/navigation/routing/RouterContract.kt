package com.copperleaf.ballast.navigation.routing

public object RouterContract {
    public data class State<T : Route>(
        val routingTable: RoutingTable<T>,
        val backstack: Backstack<T> = emptyList(),
    ) : Backstack<T> by backstack {
        override fun toString(): String {
            return """|
                |RouterContract(backstack=[
                |    ${backstack.joinToString(separator = ",\n    ")}
                |])
            """.trimMargin()
        }
    }

    public abstract class Inputs<T : Route> {
        public abstract fun BackstackNavigator<T>.navigate()

        /**
         * Navigate to the target [destination]. This destination may optionally be given a [tag], which will be pushed
         * into the backstack before the [destination], which may layer be used to pop several screens back until this
         * tag.
         */
        public data class GoToDestination<T : Route>(
            val destination: String,
            val extraAnnotations: Set<RouteAnnotation> = emptySet(),
        ) : Inputs<T>() {
            override fun BackstackNavigator<T>.navigate() {
                addToTop(destination, extraAnnotations)
            }

            override fun toString(): String {
                return "GoToDestination($destination)"
            }
        }

        /**
         * Navigate to the target [destination]. This destination may optionally be given a [tag], which will be pushed
         * into the backstack before the [destination], which may layer be used to pop several screens back until this
         * tag.
         */
        public data class ReplaceTopDestination<T : Route>(
            val destination: String,
            val extraAnnotations: Set<RouteAnnotation> = emptySet(),
        ) : Inputs<T>() {
            override fun BackstackNavigator<T>.navigate() {
                goBack()
                addToTop(destination, extraAnnotations)
            }

            override fun toString(): String {
                return "ReplaceTopDestination($destination)"
            }
        }

        /**
         * Navigate 1 destination backward in the backstack. See [goBack]
         */
        public class GoBack<T : Route> : Inputs<T>() {
            override fun BackstackNavigator<T>.navigate() {
                goBack()
            }

            override fun toString(): String {
                return "GoBack()"
            }
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
    }

    public sealed class Events<T : Route> {

        /**
         * Reports a change that was made to the backstack. []
         */
        public data class BackstackChanged<T : Route>(
            val backstack: Backstack<T>,
        ) : Events<T>() {
            override fun toString(): String {
                return "BackstackChanged(${backstack.currentDestinationOrNotFound})"
            }
        }

        /**
         * The router attempted to navigate backward, but the backstack was already empty. Typically, this would be a
         * request to either exit the app, to navigate back to the start destination.
         */
        public class BackstackEmptied<T : Route> : Events<T>() {
            override fun toString(): String {
                return "OnBackstackEmptied()"
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is BackstackEmptied<*>) return false
                return true
            }

            override fun hashCode(): Int {
                return this::class.hashCode()
            }
        }

        /**
         * The router attempted to navigate, but no change was actually made to the backstack.
         */
        public class NoChange<T : Route> : Events<T>() {
            override fun toString(): String {
                return "NoChange()"
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is NoChange<*>) return false
                return true
            }

            override fun hashCode(): Int {
                return this::class.hashCode()
            }
        }
    }
}


/*

I'm glad to hear the migration has been going well for you! Ballast was very intentionally created to be easier to use
than those other options, so that you spend less time fighting the framework and more time building out your app logic.
So I'm glad to hear you're finding that to be true in your application, too!

> Do you handle multiple nav args in any of your samples?

Multiple nav args works just the same as having a single one. All the dynamic parameters in the route format can be used
together as-needed, and those values will all be available with the delegate functions (`val sort: String? by optionalStringQuery()` for example).

> Is there any way of accessing the router further the back stack we get

The `Backstack<T>` is just a typealias to `List<Destination<T>>`, which is the entire list of entries in the backstack.
The functions like `Backstack<T>.renderCurrentDestination()` are just extension functions which generally operate on the
last entry in the list (the top of the stack), but you're free to inspect the other entries in the backstack from within
your routes or anywhere else.

> Or the only way is passing for example `onGoBack: () -> Unit`

This is more a question of how Ballast should be used, more generally. You certainly could pass the entire Router object
into your UI, but that couples your UI to the Ballast library, which should generally be avoided.

Passing callback lambdas like `onGoBack: () -> Unit` is the most future-proof way to go, but you can also pass the
RouterContract.Inputs up through those lambdas as well, to make it a bit easier to see the specific navigation happening
within your components: `postInput: (RouterContract.Inputs<T>) -> Unit`

 */
