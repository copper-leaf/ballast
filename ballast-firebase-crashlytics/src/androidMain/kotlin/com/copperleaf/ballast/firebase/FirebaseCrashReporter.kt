package com.copperleaf.ballast.firebase

import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.crashreporting.CrashReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore
import com.google.firebase.crashlytics.ktx.KeyValueBuilder
import com.google.firebase.crashlytics.ktx.setCustomKeys

public class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {

    override fun logInput(viewModelName: String, input: Any) {
        crashlytics.setCustomKeys {
            key(Keys.ViewModelName, viewModelName)
            key(
                Keys.InputType,
                "${viewModelName}.${input::class.java.simpleName}"
            )
        }
        crashlytics.log("${viewModelName}.${input}")
    }

    override fun recordInputError(viewModelName: String, input: Any, throwable: Throwable) {
        onError(viewModelName, "Input", throwable, true) {
            key(Keys.InputType, "$viewModelName.${input::class.java.simpleName}")
        }
    }

    override fun recordEventError(viewModelName: String, event: Any, throwable: Throwable) {
        onError(viewModelName, "Event", throwable, true) {
            key(Keys.EventType, "$viewModelName.${event::class.java.simpleName}")
        }
    }

    override fun recordSideJobError(viewModelName: String, key: String, throwable: Throwable) {
        onError(viewModelName, "SideJob", throwable, true) {
            key(Keys.SideJobKey, "${viewModelName}.$key")
        }
    }

    override fun recordUnhandledError(viewModelName: String, throwable: Throwable) {
        onError(viewModelName, "Unknown", throwable, false) {
        }
    }

    private fun onError(
        viewModelName: String,
        type: String,
        throwable: Throwable,
        handled: Boolean,
        extraKeys: KeyValueBuilder.() -> Unit,
    ) {
        crashlytics.setCustomKeys {
            key(Keys.ViewModelName, viewModelName)
            key(Keys.ExceptionType, type)
            extraKeys()
        }
        crashlytics.recordException(BallastCrashlyticsException(throwable, handled))
    }

    internal object Keys {
        const val ViewModelName = "ViewModelName"
        const val InputType = "InputType"
        const val EventType = "EventType"
        const val SideJobKey = "SideJobKey"
        const val ExceptionType = "ExceptionType"
    }
}
