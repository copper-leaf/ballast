package com.copperleaf.ballast.examples.router

import androidx.fragment.app.Fragment
import com.copperleaf.ballast.examples.R
import com.copperleaf.ballast.examples.ui.FloatingFragment
import com.copperleaf.ballast.examples.ui.MainActivity
import com.copperleaf.ballast.examples.ui.bgg.BggFragment
import com.copperleaf.ballast.examples.ui.counter.CounterFragment
import com.copperleaf.ballast.examples.ui.home.HomeFragment
import com.copperleaf.ballast.examples.ui.kitchensink.KitchenSinkFragment
import com.copperleaf.ballast.examples.ui.scorekeeper.ScorekeeperFragment
import com.copperleaf.ballast.examples.ui.sync.SyncFragment
import com.copperleaf.ballast.examples.ui.undo.UndoFragment
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.Floating
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.currentDestinationOrThrow
import com.copperleaf.ballast.navigation.toBundle
import com.copperleaf.ballast.navigation.vm.RouterEventHandler
import com.copperleaf.ballast.navigation.vm.RouterEventHandlerScope

class BallastExamplesRouterEventHandler(
    private val activity: MainActivity,
) : RouterEventHandler<BallastExamples> {

    private val floatingWindowStack: MutableList<Pair<Destination.Match<BallastExamples>, FloatingFragment>> =
        mutableListOf()

    private fun getFragment(
        route: BallastExamples,
    ): Class<out Fragment> = when (route) {
        BallastExamples.Home -> HomeFragment::class.java
        BallastExamples.Counter -> CounterFragment::class.java
        BallastExamples.Scorekeeper -> ScorekeeperFragment::class.java
        BallastExamples.Sync -> SyncFragment::class.java
        BallastExamples.Undo -> UndoFragment::class.java
        BallastExamples.ApiCall -> BggFragment::class.java
        BallastExamples.KitchenSink -> KitchenSinkFragment::class.java
    }

    override suspend fun RouterEventHandlerScope<BallastExamples>.handleEvent(
        event: RouterContract.Events<BallastExamples>
    ) = when (event) {
        is RouterContract.Events.BackstackChanged -> {
            // perform a fragment transaction
            val currentDestination = event.backstack.currentDestinationOrThrow
            val fragment = getFragment(currentDestination.originalRoute)
            val args = currentDestination.toBundle()

            // close any floating windows that are not in the backstack
            val floatingWindowsNoLongerInBackstack = floatingWindowStack.filter { (destination, _) ->
                event.backstack.none { it === destination }
            }

            floatingWindowsNoLongerInBackstack.forEach { floatingDestinationEntry ->
                val indexOfDestinationWithOpenFloatingWindow = floatingWindowStack.indexOf(floatingDestinationEntry)

                if (indexOfDestinationWithOpenFloatingWindow >= 0) {
                    floatingWindowStack[indexOfDestinationWithOpenFloatingWindow].second.dismiss()
                    floatingWindowStack.removeAt(indexOfDestinationWithOpenFloatingWindow)
                }
            }

            // display the current destination
            if (currentDestination.annotations.any { it is Floating }) {
                // wrap the content in a FloatingFragment and save it to the floatingWindowStack so it can be dismissed
                // on later backstack changes
                val floatingWindow = FloatingFragment.create(fragment, args)
                floatingWindowStack += (currentDestination to floatingWindow)

                floatingWindow.show(
                    activity.supportFragmentManager,
                    null,
                )
            } else {
                // just display the fragment normally
                activity
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment, args)
                    .commit()
            }

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
