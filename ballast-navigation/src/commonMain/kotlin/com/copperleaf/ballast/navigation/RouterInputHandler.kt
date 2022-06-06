package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.MissingDestination
import com.copperleaf.ballast.navigation.routing.NavToken
import com.copperleaf.ballast.navigation.routing.Tag
import com.copperleaf.ballast.navigation.routing.findMatch

public class RouterInputHandler : InputHandler<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State> {
    override suspend fun InputHandlerScope<
        RouterContract.Inputs,
        RouterContract.Events,
        RouterContract.State>.handleInput(
        input: RouterContract.Inputs
    ): Unit = when (input) {
        is RouterContract.Inputs.GoToDestination -> {
            val matchedDestination = getCurrentState().navGraph.findMatch(input.destination)

            val eventsToSend = mutableListOf<RouterContract.Events>()

            val toAppendToBackstack: List<NavToken> = if (matchedDestination == null) {
                val unmatchedDestination: MissingDestination = MissingDestination(input.destination)
                eventsToSend += RouterContract.Events.DestinationNotFound(unmatchedDestination)

                listOf(unmatchedDestination)
            } else if (input.tag != null) {
                val declaredTag = Tag(input.tag)
                eventsToSend += RouterContract.Events.TagPushed(declaredTag)
                eventsToSend += RouterContract.Events.DestinationPushed(matchedDestination)

                listOf(declaredTag, matchedDestination)
            } else {
                eventsToSend += RouterContract.Events.DestinationPushed(matchedDestination)

                listOf(matchedDestination)
            }

            updateState {
                it.copy(backstack = it.backstack.dropLastWhile { it is MissingDestination } + toAppendToBackstack)
            }

            eventsToSend.forEach { postEvent(it) }
        }
        is RouterContract.Inputs.GoBack -> {
            val currentState = getCurrentState()
            if (!canGoBackOneStep(currentState)) {
                // error, backstack was empty
                error("Backstack was empty, cannot go back")
            } else {
                val eventsToSend = mutableListOf<RouterContract.Events>()
                val updatedState = goBackOneStep(currentState, eventsToSend)

                updateState { updatedState }
                eventsToSend.forEach { postEvent(it) }
            }
        }
    }

    private fun canGoBackOneStep(
        currentState: RouterContract.State,
    ): Boolean {
        return currentState.backstack.isNotEmpty()
    }

    private fun goBackOneStep(
        currentState: RouterContract.State,
        eventsToSend: MutableList<RouterContract.Events>,
    ): RouterContract.State {
        val backstack = currentState.backstack.toMutableList()

        val lastDestination = backstack.removeLast() as Destination
        if (backstack.lastOrNull() is Tag) {
            // remove the tag, too
            val removedTag = backstack.removeLast() as Tag
            val previousTag = backstack.lastOrNull { it is Tag } as? Tag

            eventsToSend += RouterContract.Events.TagPopped(removedTag, previousTag)
        }

        val previousDestination = backstack.lastOrNull() as? Destination?
        if (previousDestination == null) {
            // after removing the last destination, and any optional tag before it, the backstack is now empty
            eventsToSend.add(0, RouterContract.Events.DestinationPopped(lastDestination, previousDestination))
            eventsToSend.add(RouterContract.Events.OnBackstackEmptied)
        } else {
            eventsToSend.add(0, RouterContract.Events.DestinationPopped(lastDestination, previousDestination))
        }

        return currentState.copy(backstack = backstack.toList())
    }
}
