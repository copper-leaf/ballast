@file:Suppress("ExtractKtorModule")

package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.debugger.BALLAST_VERSION_HEADER
import com.copperleaf.ballast.debugger.CONNECTION_ID_HEADER
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract
import com.copperleaf.ballast.debugger.versions.ClientModelSerializer
import com.copperleaf.ballast.debugger.versions.ClientVersion
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerActionV4
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4
import io.github.copper_leaf.ballast_debugger_server.BALLAST_VERSION
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
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
import org.slf4j.event.Level

public class BallastDebuggerServerConnection(
    private val logger: BallastLogger,
    private val settings: BallastDebuggerServerSettings,
    private val outgoingActions: SharedFlow<BallastDebuggerActionV4>,
    private val postInput: suspend (DebuggerServerContract.Inputs) -> Unit,
) {
    public suspend fun runServer() {
        withContext(Dispatchers.IO) {
            embeddedServer(CIO, port = settings.debuggerServerPort) {
                install(WebSockets)
                install(CallLogging) {
                    level = Level.TRACE
                }
                install(CORS) {
                    anyHost()
                }

                routing {
                    get("/") {
                        call.respondText("Ballast Debugger\nVersion: $BALLAST_VERSION")
                    }

                    webSocket("/ballast/debugger") {
                        val connectionId = call.request.headers[CONNECTION_ID_HEADER]
                            ?: call.parameters[CONNECTION_ID_HEADER]
                            ?: ""
                        val connectionBallastVersion = call.request.headers[BALLAST_VERSION_HEADER]
                            ?: call.parameters[BALLAST_VERSION_HEADER]
                            ?: ""

                        // notify that a connection was started
                        postInput(
                            DebuggerServerContract.Inputs.ConnectionEstablished(
                                connectionId = connectionId,
                                connectionBallastVersion = connectionBallastVersion,
                            )
                        )

                        val parsedVersion = ClientVersion.parse(connectionBallastVersion)
                        logger.debug("Incoming connection. Client version: $parsedVersion")

                        val modelMapper = ClientVersion.getSerializer(parsedVersion)
                        if (modelMapper.supported) {
                            logger.debug("Client version $parsedVersion is supported: $modelMapper")
                            // get the mapper for a particular version of the API, to allow clients with different '
                            // versions to connect to the same debugger server
                            joinAll(
                                processOutgoing(modelMapper, connectionId),
                                processIncoming(modelMapper),
                            )
                            logger.debug("Client version at $parsedVersion is finished")
                        } else {
                            // otherwise, drop the connection immediately, the client's version in incompatible
                            logger.debug("Client version $parsedVersion is not supported")
                        }
                    }
                }
            }.start(wait = true)
        }
    }

    private fun WebSocketServerSession.processOutgoing(
        clientModelMapper: ClientModelSerializer<BallastDebuggerEventV4, BallastDebuggerActionV4>,
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

    private fun WebSocketServerSession.processIncoming(
        clientModelMapper: ClientModelSerializer<BallastDebuggerEventV4, BallastDebuggerActionV4>,
    ): Job {
        return incoming
            .receiveAsFlow()
            .filterIsInstance<Frame.Text>()
            .onEach {
                val text = it.readText()
                try {
                    clientModelMapper
                        .mapIncoming(text)
                        .let { DebuggerServerContract.Inputs.DebuggerEventReceived(it) }
                        .let { postInput(it) }
                } catch (e: Exception) {
                    // ignore
                    logger.error(e)
                }
            }
            .launchIn(this)
    }
}
