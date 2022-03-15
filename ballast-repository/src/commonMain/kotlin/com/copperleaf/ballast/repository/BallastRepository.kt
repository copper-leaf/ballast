package com.copperleaf.ballast.repository

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.repository.bus.EventBus
import com.copperleaf.ballast.repository.bus.EventBusEventHandler
import com.copperleaf.ballast.repository.cache.Cached
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

/**
 * A Ballast Repository allows one to use the same Ballast MVI pattern for implementing an app's Repository layer. This
 * is the layer of your application that acts as the bridge between the UI/ViewModel layer, and the API layer. The
 * Repository is responsible for making API calls, converting API models to "cleaner" or "safer" representations that
 * are easier to use in the UI, caching app-wide data, and maintaining any state or background tasks that live longer
 * than a single screen.
 *
 * An application built on a Ballast UI and Repository layer would use Ballast for fetching and caching data using the
 * [Cached] feature, and UI ViewModels simple observe the state of those Cached objects as they are updated in the
 * background.
 *
 * Unlike a typical UI Ballast ViewModel, a Ballast Repository uses a [FifoInputStrategy] by default, so that many
 * Inputs can be dispatched to the Repository, and none of them will be dropped, but they will all be processed
 * eventually. One should take care to not block the Repository queue, however, but instead move long-running work into
 * a "side effect" and posting inputs back to the Repository to update the state as it changes from the application
 * background.
 *
 * Also unlike a Ballast ViewModel, a Ballast Repository doesn't have it's own [EventHandler], but rather uses its
 * EventHandler to communicate with other Repositories through an [EventBus]. A Repository can dispatch any object
 * through its [InputHandlerScope.postEvent], which will get delivered to any other Repositories listening for that
 * Input. This is commonly used to directly send Inputs to other Repositories, but you may wish to use common "tokens"
 * to request some general functionality that each Repository maps to one of its own inputs (such as a "clear cache"
 * or "refresh all caches" action). One must take care that Inputs sent to the EventBus do not create a loop.
 * Additionally, the bus is implemented with a [SharedFlow], so you should expect that each Input sent may be processed
 * more than once, if multiple Repositories are listening for it. For this reason, you can expect that sending a
 * discrete Input of another Repository's Contract will only be processed by that repository, while a more generic
 * "action token" will be processed by all Repositories, but it is your responsibility to ensure that is true of your
 * application's Repository layer.
 */
public abstract class BallastRepository<Inputs : Any, State : Any>(
    coroutineScope: CoroutineScope,
    eventBus: EventBus,
    configBuilder: BallastViewModelConfiguration.Builder,
) : BasicViewModel<Inputs, Any, State>(
    coroutineScope = coroutineScope,
    config = configBuilder
        .apply { inputStrategy = FifoInputStrategy() }
        .build(),
    eventHandler = EventBusEventHandler(eventBus),
)
