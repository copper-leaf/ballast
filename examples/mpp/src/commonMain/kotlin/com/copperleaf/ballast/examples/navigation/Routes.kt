package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.navigation.routing.RoutingTable

object Routes : RoutingTable(prefix = "/app") {
    val Main = route("/", isInitialRoute = true)
    val Counter = route("/counter")
    val BoardGameGeek = route("/bgg")
    val Scorekeeper = route("/scorekeeper")
    val KitchenSink = route("/kitchen-sink")
}
