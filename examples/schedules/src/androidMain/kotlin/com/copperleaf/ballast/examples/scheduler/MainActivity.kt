package com.copperleaf.ballast.examples.scheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifications.notify(
            title = "Ballast Scheduler",
            message = "App Launch"
        )

        setContent {
            SchedulerExampleUi.Content()
        }
    }
}
