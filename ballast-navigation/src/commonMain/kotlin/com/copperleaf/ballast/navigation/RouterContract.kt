package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.MissingDestination
import com.copperleaf.ballast.navigation.routing.NavGraph
import com.copperleaf.ballast.navigation.routing.NavToken
import com.copperleaf.ballast.navigation.routing.Tag

public object RouterContract {
    public data class State(
        val navGraph: NavGraph,
        val backstack: List<NavToken>,
    )

    public sealed class Inputs {
        /**
         * Navigate to the target [destination]. This destination may optionally be given a [tag], which will be pushed
         * into the backstack before the [destination], which may layer be used to pop several screens back until this
         * tag.
         */
        public data class GoToDestination(
            val destination: String,
            val tag: String? = null,
        ) : Inputs()

        /**
         * Navigate 1 destination backward in the backstack. The destination that was removed will be emitted as a
         * [Events.DestinationPopped] event.
         */
        public object GoBack : Inputs()
    }

    public sealed class Events {

        /**
         * A new destination was pushed into the backstack.
         */
        public data class DestinationPushed(val newDestination: Destination) : Events()

        /**
         * We attempted to navigate to a destination that could not be found in the NavGraph.
         */
        public data class DestinationNotFound(val newDestination: MissingDestination) : Events()

        /**
         * The router navigated backward 1 destination in the backstack. If this was the start destination,
         * [previousDestination] will be null, otherwise it will be non-null referring to the previous destination in
         * the backstack that is now resumed as the current destination.
         */
        public data class DestinationPopped(val removedDestination: Destination, val previousDestination: Destination?) : Events()

        /**
         * A new tag was pushed into the backstack.
         */
        public data class TagPushed(val newTag: Tag) : Events()

        /**
         * The router navigated backward in the backstack, and popped a tag in the process. If this was the only tag in
         * the backstack, [previousTag] will be null, otherwise it will be non-null referring to the previous tag in the
         * backstack that is now considered the current tag.
         */
        public data class TagPopped(val removedTag: Tag, val previousTag: Tag?) : Events()

        /**
         * The router attempted to navigate backward, but the backstack was already empty. Typically, this would be a
         * request to either exit the app, to navigate back to the start destination.
         */
        public object OnBackstackEmptied : Events()
    }
}
