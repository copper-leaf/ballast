package com.copperleaf.ballast.android.bgg

import android.util.Log
import com.copperleaf.ballast.android.util.BggApiImpl
import com.copperleaf.ballast.android.util.commonBuilder
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.bgg.repository.BggRepositoryImpl
import com.copperleaf.ballast.examples.bgg.ui.BggContract
import com.copperleaf.ballast.examples.bgg.ui.BggInputHandler
import com.copperleaf.ballast.forViewModel
import com.copperleaf.ballast.repository.bus.EventBusImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.EmptyCoroutineContext

val bggRepository by lazy {
    BggRepositoryImpl(
        coroutineScope = CoroutineScope(EmptyCoroutineContext),
        eventBus = EventBusImpl(),
        configBuilder = commonBuilder(),
        api = BggApiImpl(
            HttpClient(OkHttp) {
                install(Logging) {
                    level = LogLevel.BODY
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.i("Ktor", message)
                        }
                    }
                }
            }
        )
    )
}

@ExperimentalCoroutinesApi
class BggViewModel : AndroidViewModel<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State>(
    config = commonBuilder()
        .forViewModel(
            initialState = BggContract.State(),
            inputHandler = BggInputHandler(bggRepository),
            name = "BGG",
        ),
)
