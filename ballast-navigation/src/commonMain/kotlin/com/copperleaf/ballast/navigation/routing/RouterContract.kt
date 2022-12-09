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
