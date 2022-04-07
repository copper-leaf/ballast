package com.copperleaf.ballast.examples

import android.app.Application
import com.copperleaf.ballast.examples.util.AndroidInjector
import com.copperleaf.ballast.examples.util.AndroidInjectorImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainApplication : Application() {
    lateinit var injector: AndroidInjector

    override fun onCreate() {
        super.onCreate()
        injector = AndroidInjectorImpl(
            applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
        applicationInstance = this
    }

    companion object {
        private lateinit var applicationInstance: MainApplication
        fun getInstance(): MainApplication {
            return applicationInstance
        }
    }
}
