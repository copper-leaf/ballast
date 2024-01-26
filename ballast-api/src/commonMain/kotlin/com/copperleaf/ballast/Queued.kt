package com.copperleaf.ballast

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Duration

/**
 * Represents an item in the ViewModel's main processing queue. Queued items will always be processed predicably
 * according to the rules of the [InputStrategy] provided to the [BallastViewModelConfiguration]
 * ([BallastViewModelConfiguration.inputStrategy]).
 */
public sealed class Queued<Inputs : Any, Events : Any, State : Any> {

    public abstract val deferred: CompletableDeferred<Unit>?

    /**
     * A request to forcibly set the State to a specific value.
     */
    public class RestoreState<Inputs : Any, Events : Any, State : Any>(
        public override val deferred: CompletableDeferred<Unit>?,
        public val state: State,
    ) : Queued<Inputs, Events, State>() {
        override fun toString(): String {
            return "RestoreState(state=$state)"
        }
    }

    /**
     * A request to handle an Input by sending it to the [InputHandler].
     */
    public class HandleInput<Inputs : Any, Events : Any, State : Any>(
        public override val deferred: CompletableDeferred<Unit>?,
        public val input: Inputs,
    ) : Queued<Inputs, Events, State>() {
        override fun toString(): String {
            return "HandleInput(input=$input)"
        }
    }

    /**
     * A request to gracefully shut down the ViewModel. This will attempt to let it finish processing any Inputs
     * currently in the Queue, as well as any Events in the output Channel. Additionally, sideJobs will be given a
     * [gracePeriod] during which they will be allowed to continue processing and sending Inputs, which will also be
     * processed before fully shutting down.
     *
     * Once the VM has been shut down gracefully, its coroutineScope will be cancelled, and the ViewModel and its
     * related scope will no longer be usable. The parent coroutine scope should have a [SupervisorJob] to ensure the
     * parent scopes do not also get cancelled by this action.
     */
    public class ShutDownGracefully<Inputs : Any, Events : Any, State : Any>(
        public override val deferred: CompletableDeferred<Unit>?,
        public val gracePeriod: Duration,
    ) : Queued<Inputs, Events, State>() {
        override fun toString(): String {
            return "ShutDownGracefully(gracePeriod=$gracePeriod)"
        }
    }
}
