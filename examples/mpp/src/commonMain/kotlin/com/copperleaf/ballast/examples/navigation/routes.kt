package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.navigation.routing.Route

object Routes {
    val Main = Route("/app")
    val Counter = Route("/app/counter")
    val BoardGameGeek = Route("/app/bgg")
    val Scorekeeper = Route("/app/scorekeeper")
    val KitchenSink = Route("/app/kitchen-sink")
}
