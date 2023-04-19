package com.copperleaf.ballast.analytics

/**
 * An adapter for converting Inputs to data sent to an [AnalyticsTracker].
 *
 * @see [DefaultAnalyticsAdapter] for a default implementation
 */
public interface AnalyticsAdapter<Inputs : Any, Events : Any, State : Any> {

    /**
     * Return true if [input] should be tracked with Analytics, false if it should be ignored
     */
    public fun shouldTrackInput(input: Inputs): Boolean

    /**
     * Get an identifier from the [input] for tracking an analytics event. Corresponds to `eventId` in
     * [AnalyticsTracker.trackAnalyticsEvent].
     */
    public fun getEventIdForInput(input: Inputs): String

    /**
     * Get an identifier from the [input] for tracking an analytics event. Corresponds to `eventParameters` in
     * [AnalyticsTracker.trackAnalyticsEvent].
     */
    public fun getEventParametersForInput(viewModelName: String, input: Inputs): Map<String, String>
}
