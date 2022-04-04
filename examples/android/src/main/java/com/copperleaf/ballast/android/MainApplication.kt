package com.copperleaf.ballast.android

import android.app.Application
import android.content.Context

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = getApplicationContext()
        /* If you has other classes that need context object to initialize when application is created,
         you can use the appContext here to process. */
    }

    companion object {
        private var appContext: Context? = null
        fun getInstance(): Context {
            return appContext!!
        }
    }
}
