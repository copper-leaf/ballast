package com.copperleaf.ballast.debugger

import com.benasher44.uuid.uuid4
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.models.debuggerEventJson
import com.copperleaf.ballast.debugger.models.getActualValue
import com.copperleaf.ballast.debugger.models.serialize
import com.copperleaf.ballast.debugger.models.updateConnection
import com.copperleaf.ballast.debugger.models.updateViewModel
import com.copperleaf.ballast.debugger.models.updateWithDebuggerEvent
import io.github.copper_leaf.ballast_debugger.BALLAST_VERSION
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.ExperimentalTime

@ExperimentalTime
public class BallastDebuggerClientConnection<out T : HttpClientEngineConfig>(
    engineFactory: HttpClientEngineFactory<T>,
    private val applicationCoroutineScope: CoroutineScope,
    private val host: String = "127.0.0.1", // 10.1.1.20 on Android
    private val port: Int = 9684,
    private val ballastVersion: String = BALLAST_VERSION,
    private val connectionId: String = generateUuid(),
    block: HttpClientConfig<T>.() -> Unit = {}
) {
    public companion object {
        public const val CONNECTION_ID_HEADER: String = "x-ballast-connection-id"
        public const val BALLAST_VERSION_HEADER: String = "x-ballast-version"
        private fun generateUuid(): String {
            return uuid4().toString()
        }
    }

    private val client: HttpClient = HttpClient(engineFactory) {
        install(WebSockets)
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        block()
    }

    private class BallastDebuggerEventWrapper(
        val notification: BallastNotification<*, *, *>?,
        val debuggerEvent: BallastDebuggerEvent?,
        val updateConnectionState: Boolean,
    )

    private val outgoingMessages = Channel<BallastDebuggerEventWrapper>(Channel.UNLIMITED, BufferOverflow.SUSPEND)
    private val incomingActions = MutableSharedFlow<BallastDebuggerAction>()
    private var waitForEvent = CompletableDeferred<Unit>()

    private var applicationState: BallastApplicationState = BallastApplicationState()
    private val uuids: MutableMap<Any, String> = mutableMapOf()

    public fun connect(): Job {
        var failedAttempts = 0
        val job = applicationCoroutineScope.launch(
            start = CoroutineStart.UNDISPATCHED,
            context = Dispatchers.Default,
        ) {
            while (true) {
                val currentTimeoutValue = when (failedAttempts) {
                    0 -> {
                        0L
                    }
                    in 1..10 -> {
                        1_000L
                    }
                    in 11..20 -> {
                        5_000L
                    }
                    in 21..30 -> {
                        30_000L
                    }
                    else -> Long.MAX_VALUE
                }

                try {
                    coroutineScope {
                        // either wait for a given timeout to reconnect, or if a new event comes in connect immediately
                        withTimeoutOrNull(currentTimeoutValue) {
                            waitForEvent.await()
                        }

                        client.webSocket(
                            method = HttpMethod.Get,
                            request = {
                                url("ws", host, port, "/ballast/debugger") {
                                    parameters[CONNECTION_ID_HEADER] = connectionId
                                    parameters[BALLAST_VERSION_HEADER] = ballastVersion
                                }
                                header(CONNECTION_ID_HEADER, connectionId)
                                header(BALLAST_VERSION_HEADER, ballastVersion)
                            }
                        ) {
                            println("Connected to Ballast debugger: $connectionId")
                            failedAttempts = 0
                            waitForEvent = CompletableDeferred()
                            joinAll(
                                heartbeat(),
                                processOutgoing(),
                                processIncoming(),
                            )
                        }
                    }
                    failedAttempts++
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    failedAttempts++
                }
            }
        }
        return job
    }

    internal fun <Inputs : Any, Events : Any, State : Any> BallastInterceptorScope<Inputs, Events, State>.connectViewModel(
        notifications: Flow<BallastNotification<Inputs, Events, State>>,
    ) {
        val processIncomingJob = applicationCoroutineScope.launch {
            incomingActions
                .mapNotNull { action ->
                    if (action.viewModelName == hostViewModelName) {
                        val isForThisViewModel = applicationState.connections
                            .firstOrNull { it.connectionId == this@BallastDebuggerClientConnection.connectionId }
                            ?.viewModels
                            ?.firstOrNull { it.viewModelName == hostViewModelName }

                        if (isForThisViewModel != null) {
                            action to isForThisViewModel
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
                .collect { (action, thisViewModel) -> handleAction(action, thisViewModel) }
        }
        applicationCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            notifications
                .collect(::acceptNotification)
        }.invokeOnCompletion { processIncomingJob.cancel() }
    }

// Impl
// ---------------------------------------------------------------------------------------------------------------------

    private suspend fun acceptNotification(notification: BallastNotification<*, *, *>) {
        outgoingMessages.send(
            BallastDebuggerEventWrapper(
                notification = notification,
                debuggerEvent = null,
                updateConnectionState = true,
            )
        )
        waitForEvent.complete(Unit)
    }

    private fun getUuid(notification: BallastNotification<*, *, *>): String {
        return when (notification) {
            is BallastNotification.InputQueued -> {
                uuids.getOrPut(notification.input) { generateUuid() }
            }
            is BallastNotification.InputAccepted -> {
                uuids.getOrPut(notification.input) { generateUuid() }
            }
            is BallastNotification.InputRejected -> {
                uuids.getOrPut(notification.input) { generateUuid() }
            }
            is BallastNotification.InputDropped -> {
                uuids.getOrPut(notification.input) { generateUuid() }
            }
            is BallastNotification.InputHandledSuccessfully -> {
                uuids.remove(notification.input) ?: generateUuid()
            }
            is BallastNotification.InputCancelled -> {
                uuids.remove(notification.input) ?: generateUuid()
            }
            is BallastNotification.InputHandlerError -> {
                uuids.remove(notification.input) ?: generateUuid()
            }

            is BallastNotification.EventQueued -> {
                uuids.getOrPut(notification.event) { generateUuid() }
            }
            is BallastNotification.EventEmitted -> {
                uuids.getOrPut(notification.event) { generateUuid() }
            }
            is BallastNotification.EventHandledSuccessfully -> {
                uuids.remove(notification.event) ?: generateUuid()
            }
            is BallastNotification.EventHandlerError -> {
                uuids.remove(notification.event) ?: generateUuid()
            }

            is BallastNotification.SideEffectStarted -> {
                uuids.getOrPut(notification.key) { generateUuid() }
            }
            is BallastNotification.SideEffectCompleted -> {
                uuids.remove(notification.key) ?: generateUuid()
            }
            is BallastNotification.SideEffectCancelled -> {
                uuids.remove(notification.key) ?: generateUuid()
            }
            is BallastNotification.SideEffectError -> {
                uuids.remove(notification.key) ?: generateUuid()
            }

            else -> generateUuid()
        }
    }

    /**
     * send a heartbeat from this connection every 5 seconds, so the UI can show when each connection is still alive
     */
    private fun DefaultClientWebSocketSession.heartbeat(): Job {
        val session = this

        return flow<Unit> {
            while (true) {
                emit(Unit)
            }
        }
            .onEach { delay(5000) }
            .onEach {
                session.send(
                    debuggerEventJson
                        .encodeToString(
                            BallastDebuggerEvent.serializer(),
                            BallastDebuggerEvent.Heartbeat(connectionId)
                        )
                        .let { Frame.Text(it) }
                )
            }
            .launchIn(this)
    }

    /**
     * send messages from the channel to the websocket
     */
    private fun DefaultClientWebSocketSession.processOutgoing(): Job {
        val session = this
        return outgoingMessages
            .receiveAsFlow()
            .onEach { message ->
                val event = if (message.notification != null) {
                    check(message.debuggerEvent == null) { "Must provide a notification or a debugger event, not both" }

                    val uuid = getUuid(message.notification)
                    message.notification.serialize(connectionId, uuid)
                } else if (message.debuggerEvent != null) {
                    message.debuggerEvent
                } else {
                    error("Must provide a notification or a debugger event, not both")
                }

                if (message.updateConnectionState) {
                    // update the local cache with the same event the client UI has, so we can request refreshed data if
                    // something has been dropped
                    applicationState = applicationState.updateConnection(connectionId) {
                        updateViewModel(event.viewModelName) {
                            // on the client, we can get the actual value from the original notification, and cache it
                            // so that it can be restored later if requested
                            val actualValue = message.notification?.getActualValue()
                            updateWithDebuggerEvent(event, actualValue)
                        }
                    }
                }

                // send the message through the websocket to the client UI, where it will be processed in the same way
                session.send(
                    debuggerEventJson
                        .encodeToString(BallastDebuggerEvent.serializer(), event)
                        .let { Frame.Text(it) }
                )
            }
            .launchIn(this)
    }

    /**
     * read messages sent from the debugger UI to the app
     */
    private fun DefaultClientWebSocketSession.processIncoming(): Job {
        return incoming
            .receiveAsFlow()
            .filterIsInstance<Frame.Text>()
            .onEach { frame ->
                val text = frame.readText()

                debuggerEventJson
                    .decodeFromString(BallastDebuggerAction.serializer(), text)
                    .let { incomingActions.emit(it) }
            }
            .launchIn(this)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <Inputs : Any, Events : Any, State : Any> BallastInterceptorScope<Inputs, Events, State>.handleAction(
        action: BallastDebuggerAction,
        thisViewModel: BallastViewModelState,
    ) {
        return when (action) {
            is BallastDebuggerAction.RequestViewModelRefresh -> {
                val currentViewModelHistory = thisViewModel.fullHistory.reversed()

                outgoingMessages.send(
                    BallastDebuggerEventWrapper(
                        notification = null,
                        debuggerEvent = BallastDebuggerEvent.RefreshViewModelStart(
                            connectionId,
                            action.viewModelName,
                        ),
                        updateConnectionState = false,
                    )
                )

                currentViewModelHistory.forEach {
                    outgoingMessages.send(
                        BallastDebuggerEventWrapper(
                            notification = null,
                            debuggerEvent = it,
                            updateConnectionState = false,
                        )
                    )
                }

                outgoingMessages.send(
                    BallastDebuggerEventWrapper(
                        notification = null,
                        debuggerEvent = BallastDebuggerEvent.RefreshViewModelComplete(
                            connectionId,
                            action.viewModelName
                        ),
                        updateConnectionState = false,
                    )
                )

                waitForEvent.complete(Unit)

                Unit
            }
            is BallastDebuggerAction.RequestResendInput -> {
                val inputToResend = thisViewModel
                    .inputs
                    .firstOrNull { it.uuid == action.inputUuid }
                    ?.actualInput as? Inputs

                if (inputToResend != null) {
                    sendToQueue(Queued.HandleInput(inputToResend))
                } else {
                }
            }
            is BallastDebuggerAction.RequestRestoreState -> {
                val stateToRestore = thisViewModel
                    .states
                    .firstOrNull { it.uuid == action.stateUuid }
                    ?.actualState as? State

                if (stateToRestore != null) {
                    sendToQueue(Queued.RestoreState(stateToRestore))
                } else {
                }
            }
        }
    }
}
