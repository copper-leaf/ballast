package com.copperleaf.ballast.internal.actors

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.Status
import com.copperleaf.ballast.internal.scopes.BallastInterceptorScopeImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

internal class InterceptorActor<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>
) {
    private val _notificationsQueue: Channel<BallastNotification<Inputs, Events, State>> =
        Channel(BUFFERED, BufferOverflow.SUSPEND)
    private val _notificationsQueueDrained: CompletableDeferred<Unit> = CompletableDeferred()

    private val _notifications: MutableSharedFlow<BallastNotification<Inputs, Events, State>> = MutableSharedFlow()

    internal fun close() {
        _notificationsQueue.close()
    }

    internal fun startInterceptorsInternal() {
        // send notifications to Interceptors
        impl.interceptors
            .forEach { interceptor ->
                val notificationFlow: Flow<BallastNotification<Inputs, Events, State>> = _notifications
                    .asSharedFlow()
                    .transformWhile {
                        emit(it)

                        val shouldStopProcessingNotifications = when (it) {
                            is BallastNotification.ViewModelStatusChanged -> {
                                it.status != Status.Cleared
                            }

                            else -> true
                        }

                        shouldStopProcessingNotifications
                    }

                with(interceptor) {
                    try {
                        BallastInterceptorScopeImpl(
                            interceptorCoroutineScope = impl.viewModelScope +
                                    SupervisorJob(impl.viewModelScope.coroutineContext.job) +
                                    impl.interceptorDispatcher,
                            logger = impl.logger,
                            hostViewModelName = impl.name,
                            hostViewModelType = impl.type,
                            initialState = impl.initialState,
                            inputActor = impl.inputActor,
                            eventActor = impl.eventActor,
                        ).start(notificationFlow)
                    } catch (e: Exception) {
                        notifyImmediate(
                            BallastNotification.InterceptorFailed(
                                impl.type,
                                impl.name,
                                interceptor,
                                e
                            )
                        )
                    }
                }
            }

        impl.interceptors
            .forEach { interceptor ->
                notifyImmediate(
                    BallastNotification.InterceptorAttached(
                        impl.type,
                        impl.name,
                        interceptor
                    )
                )
            }
    }

    internal fun startProcessingNotificationsInternal() {
        // observe and process Inputs
        impl.viewModelScope.launch {
            _notificationsQueue
                .receiveAsFlow()
                .onEach { _notifications.emit(it) }
                .flowOn(impl.sideJobsDispatcher)
                .onCompletion { _notificationsQueueDrained.complete(Unit) }
                .launchIn(this)
        }
    }

    internal suspend fun notify(value: BallastNotification<Inputs, Events, State>) {
        _notificationsQueue.send(value)
    }

    internal fun notifyImmediate(value: BallastNotification<Inputs, Events, State>) {
        _notificationsQueue.trySend(value)
    }

    internal suspend fun gracefullyShutDownNotifications() {
        // close the Notifications queue and wait for all Notifications to be handled
        _notificationsQueue.close()
        _notificationsQueueDrained.await()
    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <I : BallastInterceptor<*, *, *>> getInterceptor(key: BallastInterceptor.Key<I>): I {
        val interceptorsWithKey = impl.interceptors
            .filter {
                if (it.key == null) {
                    false
                } else {
                    it.key === key
                }
            }

        if (interceptorsWithKey.isEmpty()) {
            error("Interceptor with key '$key' is not registered to ViewModel '${impl.name}'")
        }

        if (interceptorsWithKey.size > 1) {
            error("Multiple interceptors with key '$key' are registered to ViewModel '${impl.name}'")
        }

        return interceptorsWithKey.single() as? I
            ?: error("Interceptor with key '$key' does not match the type of it key")
    }
}
