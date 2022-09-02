package com.copperleaf.ballast.debugger

import com.benasher44.uuid.uuid4
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.associate
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
import com.copperleaf.ballast.debugger.utils.now
import io.github.copper_leaf.ballast_debugger.BALLAST_VERSION
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

public class BallastDebuggerClientConnection<out T : HttpClientEngineConfig>(
    engineFactory: HttpClientEngineFactory<T>,
    private val applicationCoroutineScope: CoroutineScope,
    private val host: String = "127.0.0.1", // 10.0.2.2 on Android
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
        install(ContentNegotiation) {
            json()
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
    private val uuids: MutableMap<Any, Pair<String, LocalDateTime>> = mutableMapOf()

    public fun connect(logger: BallastLogger? = null): Job {
        var failedAttempts = 0
        val job = applicationCoroutineScope.launch(
            start = CoroutineStart.UNDISPATCHED,
            context = Dispatchers.Default,
        ) {
            while (true) {
                val currentTimeoutValue = when (failedAttempts) {
                    0 -> ZERO
                    in 0..10 -> 1.seconds
                    in 11..20 -> 5.seconds
                    in 21..30 -> 30.seconds
                    else -> Int.MAX_VALUE.seconds
                }
                failedAttempts++
                waitForEvent = CompletableDeferred()

                try {
                    coroutineScope {
                        attemptConnection(currentTimeoutValue) {
                            logger?.debug("Connected to Ballast debugger: $connectionId")
                            failedAttempts = 0
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                }
            }
        }
        return job
    }

    private suspend fun attemptConnection(currentTimeoutValue: Duration, onSuccessfulConnection: () -> Unit) {
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
            onSuccessfulConnection()
            joinAll(
                heartbeat(),
                processOutgoing(),
                processIncoming(),
            )
        }
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

    private fun getUuid(notification: BallastNotification<*, *, *>): Pair<String, LocalDateTime> {
        return notification.associate(
            cache = uuids,
            computeValueForSubject = { generateUuid() to LocalDateTime.now() },
        )
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
                            BallastDebuggerEvent.Heartbeat(
                                connectionId = connectionId,
                                connectionBallastVersion = ballastVersion,
                            )
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

                    val (uuid, timestamp) = getUuid(message.notification)
                    message.notification.serialize(connectionId, uuid, firstSeen = timestamp, now = LocalDateTime.now())
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
                            action.viewModelName,
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
                    sendToQueue(Queued.HandleInput(null, inputToResend))
                } else {
                }
            }

            is BallastDebuggerAction.RequestRestoreState -> {
                val stateToRestore = thisViewModel
                    .states
                    .firstOrNull { it.uuid == action.stateUuid }
                    ?.actualState as? State

                if (stateToRestore != null) {
                    sendToQueue(Queued.RestoreState(null, stateToRestore))
                } else {
                }
            }
        }
    }
}
