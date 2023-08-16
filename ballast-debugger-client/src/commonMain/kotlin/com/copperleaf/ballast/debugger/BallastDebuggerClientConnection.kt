package com.copperleaf.ballast.debugger

import com.benasher44.uuid.uuid4
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.associate
import com.copperleaf.ballast.debugger.models.BallastApplicationState
import com.copperleaf.ballast.debugger.models.BallastViewModelState
import com.copperleaf.ballast.debugger.models.debuggerEventJson
import com.copperleaf.ballast.debugger.models.getActualValue
import com.copperleaf.ballast.debugger.utils.now
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerActionV4
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4
import io.github.copper_leaf.ballast_debugger_client.BALLAST_VERSION
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
import io.ktor.http.ContentType
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
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

    private val outgoingMessages =
        Channel<BallastDebuggerOutgoingEventWrapper<*, *, *>>(Channel.UNLIMITED, BufferOverflow.SUSPEND)
    private val incomingActions = MutableSharedFlow<BallastDebuggerActionV4>()
    private var waitForEvent = CompletableDeferred<Unit>()

    private var applicationState: BallastApplicationState = BallastApplicationState()
    private val uuids: MutableMap<Any, Pair<String, LocalDateTime>> = mutableMapOf()

    private val connections: MutableList<BallastDebuggerViewModelConnection<*, *, *>> = mutableListOf()

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
                        attemptConnection(logger, currentTimeoutValue) {
                            logger?.debug("Connected to Ballast debugger: $connectionId")
                            failedAttempts = 0
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    logger?.debug("Connection attempt failed: $connectionId")
                    logger?.error(t)
                }
            }
        }
        return job
    }

    private suspend fun attemptConnection(
        logger: BallastLogger?,
        currentTimeoutValue: Duration,
        onSuccessfulConnection: () -> Unit
    ) {
        // either wait for a given timeout to reconnect, or if a new event comes in connect immediately
        withTimeoutOrNull(currentTimeoutValue) {
            waitForEvent.await()
        }

        logger?.debug("Attempting to connect to Ballast debugger: $connectionId")
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
                heartbeat(logger),
                processOutgoing(logger),
                processIncoming(logger),
            )
        }
    }

    internal fun <Inputs : Any, Events : Any, State : Any> BallastInterceptorScope<Inputs, Events, State>.connectViewModel(
        viewModelConnection: BallastDebuggerViewModelConnection<Inputs, Events, State>
    ) {
        val processIncomingJob = applicationCoroutineScope.launch {
            connections.add(viewModelConnection)

            incomingActions
                .mapNotNull { action ->
                    if (action.viewModelName == hostViewModelName) {
                        val isForThisViewModel = applicationState.connections
                            .firstOrNull { it.connectionId == this@BallastDebuggerClientConnection.connectionId }
                            ?.viewModels
                            ?.filter { it.viewModelName == hostViewModelName }

                        if (isForThisViewModel != null) {
                            action to isForThisViewModel
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }
                .collect { (action, thisViewModelList) ->
                    thisViewModelList.forEach { thisViewModel ->
                        try {
                            handleAction(viewModelConnection, action, thisViewModel)
                        } catch (e: Throwable) {
                            // ignore for now
                        }
                    }
                }
        }
        applicationCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            viewModelConnection
                .notifications
                .collect { acceptNotification(it, viewModelConnection) }
        }.invokeOnCompletion { processIncomingJob.cancel() }
    }

// Impl
// ---------------------------------------------------------------------------------------------------------------------

    private suspend fun <Inputs : Any, Events : Any, State : Any> acceptNotification(
        notification: BallastNotification<Inputs, Events, State>,
        viewModelConnection: BallastDebuggerViewModelConnection<Inputs, Events, State>
    ) {
        outgoingMessages.send(
            BallastDebuggerOutgoingEventWrapper(
                connection = viewModelConnection,
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
    private fun DefaultClientWebSocketSession.heartbeat(logger: BallastLogger?): Job {
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
                            BallastDebuggerEventV4.serializer(),
                            BallastDebuggerEventV4.Heartbeat(
                                connectionId = connectionId,
                                connectionBallastVersion = ballastVersion,
                            )
                        )
                        .let { Frame.Text(it) }
                )
            }
            .catch {
                logger?.debug("error processing heartbeat: ")
                logger?.error(it)
            }
            .launchIn(this)
    }

    /**
     * send messages from the channel to the websocket
     */
    private fun DefaultClientWebSocketSession.processOutgoing(logger: BallastLogger?): Job {
        val session = this
        return outgoingMessages
            .receiveAsFlow()
            .onEach { message ->
                val event = if (message.notification != null) {
                    check(message.debuggerEvent == null) { "Must provide a notification or a debugger event, not both" }

                    val (uuid, timestamp) = getUuid(message.notification!!)
                    message.serialize(
                        connectionId = connectionId,
                        uuid = uuid,
                        firstSeen = timestamp,
                        now = LocalDateTime.now(),
                    )
                } else if (message.debuggerEvent != null) {
                    message.debuggerEvent
                } else {
                    error("Must provide a notification or a debugger event, not both")
                }

                if (message.updateConnectionState) {
                    // update the local cache with the same event the client UI has, so we can request refreshed data if
                    // something has been dropped
                    applicationState = applicationState.updateConnection(connectionId) {
                        updateViewModel(event!!.viewModelName) {
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
                        .encodeToString(BallastDebuggerEventV4.serializer(), event!!)
                        .let { Frame.Text(it) }
                )
            }
            .catch {
                logger?.debug("error processing outgoing: ")
                logger?.error(it)
            }
            .launchIn(this)
    }

    /**
     * read messages sent from the debugger UI to the app
     */
    private fun DefaultClientWebSocketSession.processIncoming(logger: BallastLogger?): Job {
        return incoming
            .receiveAsFlow()
            .filterIsInstance<Frame.Text>()
            .onEach { frame ->
                val text = frame.readText()

                debuggerEventJson
                    .decodeFromString(BallastDebuggerActionV4.serializer(), text)
                    .let { incomingActions.emit(it) }
            }
            .catch {
                logger?.debug("error processing incoming: ")
                logger?.error(it)
            }
            .launchIn(this)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <Inputs : Any, Events : Any, State : Any> BallastInterceptorScope<Inputs, Events, State>.handleAction(
        viewModelConnection: BallastDebuggerViewModelConnection<Inputs, Events, State>,
        action: BallastDebuggerActionV4,
        thisViewModel: BallastViewModelState,
    ) {
        return when (action) {
            is BallastDebuggerActionV4.RequestViewModelRefresh -> {
                val currentViewModelHistory = thisViewModel.fullHistory.reversed()

                outgoingMessages.send(
                    BallastDebuggerOutgoingEventWrapper(
                        connection = viewModelConnection,
                        notification = null,
                        debuggerEvent = BallastDebuggerEventV4.RefreshViewModelStart(
                            connectionId,
                            action.viewModelName,
                        ),
                        updateConnectionState = false,
                    )
                )

                currentViewModelHistory.forEach {
                    outgoingMessages.send(
                        BallastDebuggerOutgoingEventWrapper(
                            connection = viewModelConnection,
                            notification = null,
                            debuggerEvent = it,
                            updateConnectionState = false,
                        )
                    )
                }

                outgoingMessages.send(
                    BallastDebuggerOutgoingEventWrapper(
                        connection = viewModelConnection,
                        notification = null,
                        debuggerEvent = BallastDebuggerEventV4.RefreshViewModelComplete(
                            connectionId,
                            action.viewModelName,
                        ),
                        updateConnectionState = false,
                    )
                )

                waitForEvent.complete(Unit)

                Unit
            }

            is BallastDebuggerActionV4.RequestResendInput -> {
                val inputToResend = thisViewModel
                    .inputs
                    .firstOrNull { it.uuid == action.inputUuid }
                    ?.actualInput as? Inputs

                if (inputToResend != null) {
                    sendToQueue(Queued.HandleInput(null, inputToResend))
                } else {
                }
            }

            is BallastDebuggerActionV4.RequestRestoreState -> {
                val stateToRestore = thisViewModel
                    .states
                    .firstOrNull { it.uuid == action.stateUuid }
                    ?.actualState as? State

                if (stateToRestore != null) {
                    sendToQueue(Queued.RestoreState(null, stateToRestore))
                } else {
                }
            }

            is BallastDebuggerActionV4.RequestReplaceState -> {
                if (viewModelConnection.deserializeState == null) {
                    logger.info("States cannot be replaced from serialized state.")
                    return
                }

                val stateToReplaceResult = runCatching {
                    viewModelConnection.deserializeState!!(
                        ContentType.parse(action.stateContentType),
                        action.serializedState,
                    )
                }

                stateToReplaceResult.fold(
                    onSuccess = { state ->
                        sendToQueue(Queued.RestoreState(null, state))
                    },
                    onFailure = { error ->
                        logger.info("Serialized state is formatted incorrectly")
                        logger.error(error)
                    },
                )
            }
        }
    }
}
