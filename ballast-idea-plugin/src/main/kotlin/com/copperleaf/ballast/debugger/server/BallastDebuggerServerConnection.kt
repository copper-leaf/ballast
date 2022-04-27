package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection.Companion.BALLAST_VERSION_HEADER
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection.Companion.CONNECTION_ID_HEADER
import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerContract
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext

public class BallastDebuggerServerConnection(
    private val port: Int,
    private val outgoingActions: SharedFlow<BallastDebuggerAction>,
    private val postInput: suspend (DebuggerContract.Inputs) -> Unit,
) {

    public suspend fun runServer() {
        withContext(Dispatchers.IO) {
            embeddedServer(CIO, port = port) {
                install(WebSockets)

                routing {
                    get("/") {
                        call.respondText("Hello, world!")
                    }

                    webSocket("/ballast/debugger") {
                        val connectionId = call.request.headers[CONNECTION_ID_HEADER]
                            ?: call.parameters[CONNECTION_ID_HEADER]
                            ?: ""
                        val connectionBallastVersion = call.request.headers[BALLAST_VERSION_HEADER]
                            ?: call.parameters[BALLAST_VERSION_HEADER]
                            ?: ""

                        val modelMapper = ClientModelMapper.getForVersion(
                            connectionBallastVersion = connectionBallastVersion,
                        )

                        // notify that a connection was started
                        postInput(
                            DebuggerContract.Inputs.ConnectionEstablished(
                                connectionId = connectionId,
                                connectionBallastVersion = connectionBallastVersion,
                            )
                        )

                        if (modelMapper != null) {
                            // get the mapper for a particular version of the API, to allow clients with different '
                            // versions to connect to the same debugger server
                            joinAll(
                                processOutgoing(modelMapper, connectionId),
                                processIncoming(modelMapper),
                            )
                        } else {
                            // otherwise, drop the connection immediately, the client's version in incompatible
                        }
                    }
                }
            }.start(wait = true)
        }
    }

    private fun DefaultWebSocketServerSession.processOutgoing(
        clientModelMapper: ClientModelMapper,
        connectionId: String
    ): Job {
        val session = this
        return outgoingActions
            .filter { it.connectionId == connectionId }
            .onEach { message ->
                // send the message from the client UI back to the device, to request that it perform certain
                // actions on the device
                session.send(
                    clientModelMapper
                        .mapOutgoing(message)
                        .let { Frame.Text(it) }
                )
            }
            .launchIn(this)
    }

    private fun DefaultWebSocketServerSession.processIncoming(
        clientModelMapper: ClientModelMapper,
    ): Job {
        return incoming
            .receiveAsFlow()
            .filterIsInstance<Frame.Text>()
            .onEach {
                val text = it.readText()

                clientModelMapper
                    .mapIncoming(text)
                    .let { DebuggerContract.Inputs.DebuggerEventReceived(it) }
                    .let { postInput(it) }
            }
            .launchIn(this)
    }
}
