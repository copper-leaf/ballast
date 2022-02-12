package com.copperleaf.ballast.repository.cache

/**
 * A wrapper type designed to be stored within a Repository ViewModel and observed as a Flow in a Fragment ViewModel,
 * for fetching data from a remote source and caching it in memory. When data is refreshed, the previous values are kept
 * around for a nicer user experience, so the entire screen's data does not get cleared and re-populated causing
 * unwanted "jank" in the UI.
 *
 * Cached values should only be updated from a Repository. Typically, a UI ViewModel will observe a specific Cached
 * value, and the act of observing it requests the Repository start loading it. The UI ViewModel is then just a passive
 * observer of the state of the Cached value, which encapsulates all the data needed to decide when to show progress
 * loaders or success/error states of that value. When a UI ViewModel observes a Cached value, it should not need any
 * additional bookkeeping.
 */
public sealed class Cached<T : Any> {

    /**
     * The default state of a cached value. Cached values in a ViewModel or Repository State should never be null, but
     * always initialized to [Cached.NotLoaded].
     *
     * Forcing a refresh will reset the property's state back to NotLoaded, which will then call to the remote data
     * source again.
     */
    public class NotLoaded<T : Any>(public val previousCachedValue: T? = null) : Cached<T>() {
        override fun toString(): String {
            return "NotLoaded(previousCachedValue=$previousCachedValue)"
        }
    }

    /**
     * Indicates that we have started the call to the remote data source, but it has not responded or failed yet. The
     * "fetcher" coroutine is still active.
     */
    public class Fetching<T : Any>(public val cachedValue: T?) : Cached<T>() {
        override fun toString(): String {
            return "Fetching(cachedValue=$cachedValue)"
        }
    }

    /**
     * Indicates that the remote "fetcher" has sucessfully returned data and that the Repository successfully performed
     * any follow-up computation on that data.
     */
    public class Value<T : Any>(public val value: T) : Cached<T>() {
        override fun toString(): String {
            return "Value(value=$value)"
        }
    }

    /**
     * Indicates that either the remote "fetcher" function itself failed, or that it succeeded but returned bad data
     * that caused the Repository to throw an exception when processing it.
     */
    public class FetchingFailed<T : Any>(public val error: Throwable, public val cachedValue: T?) : Cached<T>() {
        override fun toString(): String {
            return "FetchingFailed(error=${error.message}, cachedValue=$cachedValue)"
        }
    }
}
