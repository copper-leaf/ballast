@file:Suppress("DEPRECATION")

package com.copperleaf.ballast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * The entry-point for attaching additional functionality to a ViewModel. As Inputs or other features get processed
 * within the ViewModel, it will emit a stream of [BallastNotification] through a [SharedFlow] to all registered
 * Interceptors.
 */
public interface BallastInterceptor<Inputs : Any, Events : Any, State : Any> {

    /**
     * This is the main entry-point to Interceptor processing, and is the only callback directly called by the Ballast
     * framework. This callback should be overriden for more complex Interceptors that need to launch their own
     * coroutines, or that need to communicate back with the ViewModel by sending [Queued] objects back to the VM's
     * main queue.
     *
     * [BallastInterceptorScope] contains [CoroutineScope] that is tied to the ViewModel's main scope, so any jobs
     * launched here will be cancelled when the VM itself is cancelled. Interceptors may also need to launch into
     * another, more global [CoroutineScope] to collect the Notifications. In that case, the [notifications] Flow will
     * be terminated automatically after sending [BallastNotification.ViewModelCleared], which would then complete that
     * job at the appropriate time. Coroutines launched here must be launched with [CoroutineStart.UNDISPATCHED] to
     * guarantee that they will start collecting from [notifications] before any Notifications are actually sent from
     * the ViewModel, so that none of them are ever dropped from the Interceptor.
     *
     * When running, the Interceptor may post [Queued] objects back to the VM's main queue. This would be useful for
     * sending Inputs back to be processed, or forcibly updating the state without going through an Input.
     *
     * Use this snippet to get started:
     *
     * ```
     * override fun BallastInterceptorScope<Inputs, Events, State>.start(
     *     notifications: Flow<BallastNotification<Inputs, Events, State>>,
     * ) {
     *     launch(start = CoroutineStart.UNDISPATCHED) {
     *         notifications.awaitViewModelStart()
     *         notifications
     *             .onEach { handleInput(it) }
     *             .collect()
     *     }
     * }
     * ```
     */
    public fun BallastInterceptorScope<Inputs, Events, State>.start(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    )

    public val key: Key<BallastInterceptor<*, *, *>>? get() = null

    /**
     * A key for accessing this Interceptor directly from a SideJob. Interceptors do not need to define a Key, but it it
     * does, the Key must be unique among all Interceptors registered to a ViewModel.
     *
     * An Interceptor's Key should conventionally be defined as an `object` on the Interceptor class names `Key`, as
     * shown in the following example:
     *
     * ```
     * public class ExampleInterceptor<Inputs : Any, Events : Any, State : Any> : BallastInterceptor<Inputs, Events, State> {
     *     public object Key : BallastInterceptor.Key<ExampleInterceptor<*, *, *>>
     *     override val key: BallastInterceptor.Key<ExampleInterceptor<*, *, *>> = ExampleInterceptor.Key
     * }
     * ```
     */
    public interface Key<out Interceptor: BallastInterceptor<*, *, *>>
}
