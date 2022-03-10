package com.copperleaf.ballast

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.StateFlow

/**
 * A base ViewModel class that enforces the MVI pattern for UI state management.
 *
 * MVI consists of 3 types of classes which interact in a unidirectional reactive loop:
 *
 * - M (Model): The single source-of-truth for all data on a single screen.
 * - V (View): The View observes changes to the Model, applying the Model State to update the UI in response.
 * - I (Intent): Inputs are changes sent from the UI back to the ViewModel, which are processed to update the Model.
 *
 * There are several important "types" that implement this MVI pattern:
 *
 * - [State] - This is the persistent state which is displayed on the UI by Compose. The State is an immutable data class,
 *      which is published to the UI through a [StateFlow]. The UI is unable to directly modify the State.
 * - [Inputs] - Since the UI cannot directly modify the State, it must send Inputs to the [BallastViewModel]. These Inputs
 *      are processed sequentially, one at a time, updating the [State] as-needed.
 * - [Events] - In addition to persistent state that is displayed directly in the UI, the result of handling an Input
 *      may require some UI-bound action to be peformed, such as navigating to a new screen or displaying an ephemeral
 *      UI element that is not necessarily bound directly to the View state. Such "notifications" are sent from the
 *      [BallastViewModel] as Events, which are processed only when the View is in a valid lifecycle state.
 *
 * The above are the main _types_ which declaratively describe the entirely of a single screen/Fragment, and are typically
 * all declared together within a single "Contract" class, with the naming convention of <ScreenName>Contract.State,
 * <ScreenName>Contract.Inputs, <ScreenName>Contract.Events. By looking only at this contract, one should be able to
 * get a general idea of all functionality available on that screen; there should be nothing happening on that screen that
 * is not captured by this contract.
 *
 * To actually run and process the MVI pattern, the following classes are needed:
 *
 * - [BallastViewModel] - Basically just a container for managing everything. It is an instance of [androidx.lifecycle.ViewModel],
 *      created and injected by Hilt into each Screen, but otherwise the other classes in this pattern do the more
 *      interesting work
 * - [InputHandler] - Any Inputs sent from the UI will eventually be given to the InputHandler to actually be processed.
 *      Inputs are always processed serially, one at a time, but within a suspending context; you're free to make API
 *      calls or run other work in the background without any risk of race conditions or other problems causes by running
 *      code in parallel. The InputHandler has several useful methods available in its DSL for processing the Input
 *      and helping ensure you're abiding by the MVI pattern properly. The InputHandler should never be privy to any
 *      Android UI- or lifecycle-bound objects, such as [Context]. It should be pure-kotlin, and run without any concern of
 *      lifecycles.
 * - [EventHandler] - The InputHandler DSL may update the State, which is sent directly to the View through a
 *      [StateFlow], but it may also emit 1-time events. These Events are sent to the EventHandler, which is also
 *      tied to the Fragment's Lifecycle and will only process Events when the Fragment is in the [Lifecycle.State.RESUMED]
 *      state. If an Event is emitted while the Fragment is not in this state, it will be queued up until the UI returns
 *      to a valid state which can handle it properly. Because the EventHandler is bound to the UI, it is able to interact
 *      with Android Android UI- or lifecycle-bound objects, and is commonly used to handle Navigation requests.
 * - [InputFilter] - When Inputs are sent to the ViewModel, they are are expected to be handled immediately. But because of
 *      the suspending, asynchronous nature of the InputHandler, it may be the case that an Input is still being processed
 *      when a new Input is received; in this case, the [InputFilter] may choose to explicitly ignore that Input, or accept
 *      it. When an Input is accepted and other is still running, the previous one will be cancelled, the State rolled back,
 *      and the new Input will start processing. The Filter is optional, but can be a very useful tool.
 *
 * The above classes implement the entirety of the MVI pattern, but because of the way everything is handled as discrete
 * classes (rather than just calling methods on the ViewModel, for example), it becomes possible to automatically inspect
 * the flow of Inputs, Events, changes to State, and capture errors, without needing any additional annotation processing
 * or anything like that. By attaching an optional [BallastInterceptor] to your BallastViewModel, you will be notified of all
 * sorts of relevant points throughout the entire data-processing flow, for purposes like logging, crash reporting, or
 * capturing analytics. Note that BallastInterceptors are entirely passive, only being notified of important actions, but cannot
 * directly change anything within the ViewModel as a result, to preserve the well-defined processing of the entire pattern.
 *
 * The entire MVI pattern is structured such that everything is well-defined, things are processed in a predictable order,
 * and you can trace the exact flow of data through the VM without necessarily needing to actually execute it and attach
 * a debugger to figure out what's happening, when. However, the very nature of everything in the pattern until this
 * point is still serial, but there may be cases where you need something to run in parallel to the VM. To accomodate
 * this, [InputHandlerScope.sideEffect] may be used to launch coroutines that run parallel to the normal MVI
 * flow, but with restrictions on what they are capable of doing in in parallel; namely side-effects cannot update the
 * State, which is key in preventing race-conditions. Instead, SideEffects should post Inputs back to the VM, carrying
 * new data and applying it to the State when it is their turn for processing. Side-effects are commonly used for
 * observing Flows.
 */
public interface BallastViewModel<Inputs : Any, Events : Any, State : Any> : SendChannel<Inputs> {

    /**
     * The name of the viewmodel, for debugging and interception purposes. Can be set manually through
     * [BallastViewModelConfiguration], or else a name will be created automatically.
     */
    public val name: String

    /**
     * The type of the viewmodel, for debugging and interception purposes. Can be set manually through
     * [BallastViewModelConfiguration], or else a name will be created automatically.
     */
    public val type: String

    /**
     * Observe the flow of states from this ViewModel
     */
    public fun observeStates(): StateFlow<State>
}
