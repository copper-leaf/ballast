package com.copperleaf.ballast.firebase

/**
 * By default, all Inputs are sent to Crashlytics for the "debug log" to help in diagnosing
 * crashes. Some Inputs may occur very frequently but contain little useful information for
 * diagnostics (such as updating TextFields), and can be excluded from the debug log by
 * annotating the class with @Ignore.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class FirebaseCrashlyticsIgnore
