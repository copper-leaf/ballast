package com.copperleaf.ballast.debugger.idea.features.debugger.router

import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouteAnnotation
import com.copperleaf.ballast.navigation.routing.RouteMatcher

enum class DebuggerRoute(
    routeFormat: String,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    Connection("/debugger/{connectionId?}"),

    ViewModelStates("/debugger/{connectionId}/vm/{viewModelName}/states"),
    ViewModelStateDetails("/debugger/{connectionId}/vm/{viewModelName}/states/{stateUuid}"),

    ViewModelInputs("/debugger/{connectionId}/vm/{viewModelName}/inputs"),
    ViewModelInputDetails("/debugger/{connectionId}/vm/{viewModelName}/inputs/{inputUuid}"),

    ViewModelEvents("/debugger/{connectionId}/vm/{viewModelName}/events"),
    ViewModelEventDetails("/debugger/{connectionId}/vm/{viewModelName}/events/{eventUuid}"),

    ViewModelSideJobs("/debugger/{connectionId}/vm/{viewModelName}/side-jobs"),
    ViewModelSideJobDetails("/debugger/{connectionId}/vm/{viewModelName}/side-jobs/{sideJobUuid}"),

    ViewModelInterceptors("/debugger/{connectionId}/vm/{viewModelName}/interceptors"),
    ViewModelInterceptorDetails("/debugger/{connectionId}/vm/{viewModelName}/interceptors/{interceptorUuid}"),

    ViewModelLogs("/debugger/{connectionId}/vm/{viewModelName}/logs"),
    ViewModelTimeline("/debugger/{connectionId}/vm/{viewModelName}/timeline"),
    ;

    override val matcher: RouteMatcher = RouteMatcher.create(routeFormat)
}
