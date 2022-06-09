package com.copperleaf.ballast.navigation.routing

public object RouterContract {
    public data class State(
        val navGraph: NavGraph,
        val backstack: List<NavToken> = emptyList(),
    ) {
        override fun toString(): String {
            return """|
                |RouterContract(backstack=[
                |    ${backstack.joinToString(separator = ",\n    ") { it.description() }}
                |])
            """.trimMargin()
        }
    }

    public abstract class Inputs {

        public abstract fun updateBackstack(
            originalState: State
        ): State

        /**
         * Navigate to the target [destination]. This destination may optionally be given a [tag], which will be pushed
         * into the backstack before the [destination], which may layer be used to pop several screens back until this
         * tag.
         */
        public data class GoToDestination(
            val destination: String,
            val tag: String? = null,
        ) : Inputs() {
            override fun updateBackstack(originalState: State): State {
                return addToTop(originalState, destination, tag)
            }

            override fun toString(): String {
                return """|
                |GoToDestination(
                |    destination=$destination,
                |    tag=$tag
                |)
                """.trimMargin()
            }
        }

        /**
         * Navigate to the target [destination]. This destination may optionally be given a [tag], which will be pushed
         * into the backstack before the [destination], which may layer be used to pop several screens back until this
         * tag.
         */
        public data class ReplaceTopDestination(
            val destination: String,
            val tag: String? = null,
        ) : Inputs() {
            override fun updateBackstack(originalState: State): State {
                return addToTop(goBack(originalState), destination, tag)
            }

            override fun toString(): String {
                return """|
                |ReplaceTopDestination(
                |    destination=$destination,
                |    tag=$tag
                |)
                """.trimMargin()
            }
        }

        /**
         * Navigate 1 destination backward in the backstack. The destination that was removed will be emitted as a
         * [Events.DestinationPopped] event.
         */
        public object GoBack : Inputs() {
            override fun updateBackstack(originalState: State): State {
                return goBack(originalState)
            }
            override fun toString(): String {
                return "GoBack()"
            }
        }
    }

    public sealed class Events {

        /**
         * Reports a change that was made to the backstack. []
         */
        public data class NewDestination(
            val backstack: List<NavToken>,
            val currentDestination: Destination?,
            val currentTag: Tag?,
        ) : Events() {
            override fun toString(): String {
                return """|
                |NewDestination(
                |    currentDestination=${currentDestination?.description()},
                |    tag=${currentTag?.description()}
                |)
                """.trimMargin()
            }
        }

        /**
         * The router attempted to navigate backward, but the backstack was already empty. Typically, this would be a
         * request to either exit the app, to navigate back to the start destination.
         */
        public object OnBackstackEmptied : Events() {
            override fun toString(): String {
                return """|
                |OnBackstackEmptied()
                """.trimMargin()
            }
        }
    }
}
