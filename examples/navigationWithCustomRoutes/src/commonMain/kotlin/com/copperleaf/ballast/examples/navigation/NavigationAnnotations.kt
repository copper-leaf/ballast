package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.navigation.routing.RouteAnnotation
import kotlin.reflect.KClass

/**
 * This file represents a set of annotations that could be used to create a KSP processor for Ballast Navigation. The
 * end-user could annotate their classes with these annotations, and the processor generates all code necessary to
 * abstract away the internal URL-based structure of it.
 */

// ---------------------------------------------------------------------------------------------------------------------

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class InitialRoute

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Route(val path: String = "")

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class PathParameter

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class QueryParameter

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class With(val annotationClass: KClass<out RouteAnnotation>, vararg val args: String)
