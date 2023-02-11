package com.copperleaf.ballast.repository.cache

import com.copperleaf.ballast.InputHandlerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Populates a cached value in a Repository ViewModel. The remote data source ([doFetch]) will only be called if the
 * current state is [NotLoaded] or [FetchingFailed].
 *
 * When [forceRefresh] is true, the state will initially be reset back to [NotLoaded], but will retain the previous
 * value in the cache. Thus, as this method runs and the state of the property changes, the UI will continue showing
 * the stale data until the remote fetch has completed or failed. This has the effect of allowing the UI to show a
 * progress indicator over the previous content, rather than removing the previous content and showing the progress
 * indicator in its place. This just makes a nicer user experience, where there is less UI "jank" when refreshing data.
 *
 * TODO: maybe add retry logic in here, to help deal with potentially flaky APIs?
 */
public suspend fun <Inputs : Any, Events : Any, State : Any, Property> InputHandlerScope<Inputs, Events, State>.fetchWithCache(
    input: Inputs,
    forceRefresh: Boolean,
    matchesPrerequisites: (State) -> Boolean = { true },
    getValue: (State) -> Cached<Property>,
    updateState: suspend (Cached<Property>) -> Inputs,
    doFetch: suspend (State) -> Property,
) {
    // VM is not ready to start fetching yet
    val currentState = getCurrentState()
    if (!matchesPrerequisites(currentState)) return

    // VM is already fetching when another request came in. If the second request is a forced refresh, cancel the first
    // by restarting the side-job. If the second reqeust is not a forced refresh, return and allow the original to
    // continue executing.
    if (getValue(currentState) is Cached.Fetching && !forceRefresh) return

    val currentStateWhenStartingSideJob = getCurrentState()

    sideJob(input::class.simpleName!!) {
        val initialValue = getValue(currentStateWhenStartingSideJob)
        val currentValueUnboxed = initialValue.getCachedOrNull()
        val currentValue = if (forceRefresh) {
            // if forcing a refresh, first mark it as not loaded (but keep the previous value for a better UI experience
            // when re-fetching)
            Cached.NotLoaded(currentValueUnboxed).also { postInput(updateState(it)) }
        } else {
            // otherwise, use the existing value as the current cached value
            initialValue
        }

        // if we have not loaded yet, have requested a forced refresh, or the previous attempt to fetch failed, try fetching
        // from the remote source now
        if (currentValue is Cached.NotLoaded<Property> || currentValue is Cached.FetchingFailed<Property>) {
            postInput(updateState(Cached.Fetching(currentValueUnboxed)))

            val result = try {
                coroutineScope { Cached.Value(doFetch(currentStateWhenStartingSideJob)) }
            } catch (t: Throwable) {
                Cached.FetchingFailed(t, currentValueUnboxed)
            }

            postInput(updateState(result))
        }
    }
}

/**
 * Populates a cached value in a Repository ViewModel. The remote data source ([doFetch]) will only be called if the
 * current state is [NotLoaded] or [FetchingFailed].
 *
 * When [forceRefresh] is true, the state will initially be reset back to [NotLoaded], but will retain the previous
 * value in the cache. Thus, as this method runs and the state of the property changes, the UI will continue showing
 * the stale data until the remote fetch has completed or failed. This has the effect of allowing the UI to show a
 * progress indicator over the previous content, rather than removing the previous content and showing the progress
 * indicator in its place. This just makes a nicer user experience, where there is less UI "jank" when refreshing data.
 *
 * TODO: maybe add retry logic in here, to help deal with potentially flaky APIs?
 */
public suspend fun <Inputs : Any, Events : Any, State : Any, Property> InputHandlerScope<Inputs, Events, State>.fetchWithCache(
    input: Inputs,
    forceRefresh: Boolean,
    matchesPrerequisites: (State) -> Boolean = { true },
    getValue: (State) -> Cached<Property>,
    updateState: suspend (Cached<Property>) -> Inputs,
    doFetch: suspend (State) -> Unit,
    observe: Flow<Property>,
) {
    // VM is not ready to start fetching yet
    val currentState = getCurrentState()
    if (!matchesPrerequisites(currentState)) return

    // VM is already fetching when another request came in. If the second request is a forced refresh, cancel the first
    // by restarting the side-job. If the second request is not a forced refresh, return and allow the original to
    // continue executing.
    if (getValue(currentState) is Cached.Fetching && !forceRefresh) return

    val currentStateWhenStartingSideJob = getCurrentState()

    sideJob(input::class.simpleName!!) {
        val initialValue = getValue(currentStateWhenStartingSideJob)
        val currentValueUnboxed = initialValue.getCachedOrNull()
        val currentValue = if (forceRefresh) {
            // if forcing a refresh, first mark it as not loaded (but keep the previous value for a better UI experience
            // when re-fetching)
            Cached.NotLoaded(currentValueUnboxed).also { postInput(updateState(it)) }
        } else {
            // otherwise, use the existing value as the current cached value
            initialValue
        }

        // if we have not loaded yet, have requested a forced refresh, or the previous attempt to fetch failed, try fetching
        // from the remote source now
        if (currentValue is Cached.NotLoaded<Property> || currentValue is Cached.FetchingFailed<Property>) {
            postInput(updateState(Cached.Fetching(currentValueUnboxed)))

            try {
                coroutineScope {
                    doFetch(currentStateWhenStartingSideJob)
                }
            } catch (t: Throwable) {
                postInput(updateState(Cached.FetchingFailed(t, currentValueUnboxed)))
            }
        }

        // start observing the remote source so that we can use it as we send along with the cached states
        observe
            .onEach { postInput(updateState(Cached.Value(it))) }
            .launchIn(this)
    }
}
