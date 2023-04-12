package com.copperleaf.ballast.firebase

/**
 * Mark an Input as one that should be tracked automatically in Firebase Analytics. Inputs
 * without this annotation are ignored.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class FirebaseAnalyticsTrackInput
