package com.copperleaf.ballast.examples

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.copperleaf.ballast.examples.bgg.BggAndroidUi
import com.copperleaf.ballast.examples.counter.CounterAndroidUi
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkAndroidUi
import com.copperleaf.ballast.examples.mainlist.MainAndroidUi
import com.copperleaf.ballast.examples.navigation.Routes
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperAndroidUi
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.navigation.routing.currentDestination

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalInjector provides MainApplication.getInstance().injector) {
                    val injector = LocalInjector.current
                    val router = remember(injector) { injector.routerViewModel() }

                    val currentScreen by router.observeStates().collectAsState()

                    when (currentScreen.currentDestination?.originalRoute) {
                        Routes.Main -> {
                            MainAndroidUi.AndroidContent()
                        }
                        Routes.Counter -> {
                            CounterAndroidUi.AndroidContent()
                        }
                        Routes.BoardGameGeek -> {
                            BggAndroidUi.AndroidContent()
                        }
                        Routes.Scorekeeper -> {
                            ScorekeeperAndroidUi.AndroidContent()
                        }
                        Routes.KitchenSink -> {
                            KitchenSinkAndroidUi.AndroidContent()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
