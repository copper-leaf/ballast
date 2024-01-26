package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection

internal expect fun platformDebuggerConnection(): BallastDebuggerClientConnection<*>

internal expect fun platformLogger(loggerName: String): BallastLogger
