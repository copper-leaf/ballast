package com.copperleaf.ballast.examples.scheduler

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.plusAssign
import com.copperleaf.schedules.R
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.time.Clock

private val lazyConnection by lazy {
    BallastDebuggerClientConnection(
        engineFactory = CIO,
        applicationCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        host = "10.0.2.2",
    ) {
        // CIO Ktor client engine configuration
    }.also { it.connect() }
}

internal actual fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder =
    apply {
        this += BallastDebuggerInterceptor(lazyConnection)
    }

internal actual fun platformLogger(loggerName: String): BallastLogger {
    return AndroidLogger(loggerName)
}

actual class Notifications {

    private val json: Json = Json.Default
    private val serializer = ListSerializer(String.serializer())
    private val preferences: SharedPreferences by lazy {
        MainApp.INSTANCE!!.getSharedPreferences("notifications", MODE_PRIVATE)
    }

    private var logs: List<String>
        get() = preferences.getString("logs", null)
            ?.let { json.decodeFromString(serializer, it) }
            ?: emptyList()
        set(value) {
            preferences
                .edit()
                .putString("logs", json.encodeToString(serializer, value))
                .apply()
        }

    actual fun notify(
        title: String,
        message: String,
    ) {
        notifyInternal(title, message, MainApp.INSTANCE!!)
    }

    actual fun getNotificationLogs(): List<String> {
        return logs
    }

    private fun notifyInternal(
        title: String,
        message: String,
        context: Context = MainApp.INSTANCE!!,
    ) = with(context) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val time = LocalTime(now.hour, now.minute, now.second)
        logs = logs + "(${now.date} - $time)\n[$title]: $message"

        val channelName = createNotificationChannel()

        val builder = NotificationCompat.Builder(this, channelName)
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define.
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(0, builder.build())
        }
    }

    private fun Context.createNotificationChannel(): String {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("ballast", "Ballast Scheduler", importance).apply {
                description = "Ballast Scheduler"
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        return "ballast"
    }
}
