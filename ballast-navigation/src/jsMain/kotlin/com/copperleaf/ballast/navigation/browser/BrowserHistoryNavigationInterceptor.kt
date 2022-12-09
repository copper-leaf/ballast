package com.copperleaf.ballast.navigation.browser

import com.copperleaf.ballast.navigation.routing.Route
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.http.parseQueryString
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.w3c.dom.PopStateEvent

// TODO: read the value of the <base> tag to determine the base URL
public class BrowserHistoryNavigationInterceptor<T : Route>(
    basePath: String? = null,
    initialRoute: T,
) : BaseBrowserNavigationInterceptor<T>(initialRoute) {
    private val basePath: String? = basePath?.trim('/')

    override fun getInitialUrl(): Url? {
        val browserPath = window.location.pathname.trimStart('/')
        val initialPath = if (basePath != null) {
            browserPath.removePrefix(basePath).trimStart('/')
        } else {
            browserPath
        }
        val initialQueryString = window.location.search.trimStart('?').takeIf { it.isNotBlank() }

        return if (initialPath.isNotBlank() || !initialQueryString.isNullOrBlank()) {
            URLBuilder()
                .apply {
                    encodedPath = initialPath
                    encodedParameters = ParametersBuilder().apply {
                        appendAll(parseQueryString(initialQueryString ?: "", decode = true))
                    }
                }
                .build()
        } else {
            null
        }
    }

    override fun watchForUrlChanges(): Flow<Url> {
        return callbackFlow {
            window.onpopstate = { event: PopStateEvent ->
                this@callbackFlow.trySend(Url(event.state.toString()))
                Unit
            }

            awaitClose {
                window.onpopstate = null
            }
        }
    }

    override fun setDestinationUrl(url: Url) {
        try {
            val previousDestination = getInitialUrl()
            if (previousDestination != url) {
                val updatedUrl = if(basePath != null) {
                    URLBuilder(url)
                        .apply {
                            encodedPath = "${basePath}/${url.encodedPath.trim('/')}"
                        }
                        .build()
                } else {
                    url
                }
                val serializedUrl = updatedUrl.encodedPathAndQuery
                window.history.pushState(serializedUrl, "", serializedUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
