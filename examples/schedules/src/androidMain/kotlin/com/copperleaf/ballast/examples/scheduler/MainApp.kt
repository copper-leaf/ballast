package com.copperleaf.ballast.examples.scheduler

import android.app.Application

public class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    public companion object {
        var INSTANCE: MainApp? = null
    }
}
