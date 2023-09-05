package com.copperleaf.ballast.navigation.browser

import com.copperleaf.ballast.navigation.internal.Uri
import com.copperleaf.ballast.navigation.routing.Route
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

    override fun getInitialUrl(): Uri? {
        val browserPath = window.location.pathname.trimStart('/')
        val initialPath = if (basePath != null) {
            browserPath.removePrefix(basePath).trimStart('/')
        } else {
            browserPath
        }
        val initialQueryString = window.location.search.trimStart('?').takeIf { it.isNotBlank() }

        return if (initialPath.isNotBlank() || !initialQueryString.isNullOrBlank()) {
            Uri.build(
                encodedPath = initialPath,
                encodedQueryString = initialQueryString,
            )
        } else {
            null
        }
    }

    override fun watchForUrlChanges(): Flow<Uri> {
        return callbackFlow {
            window.onpopstate = { event: PopStateEvent ->
                this@callbackFlow.trySend(Uri.parse(event.state.toString()))
                Unit
            }

            awaitClose {
                window.onpopstate = null
            }
        }
    }

    override fun setDestinationUrl(url: Uri) {
        try {
            val previousDestination = getInitialUrl()
            if (previousDestination != url) {
                val updatedUrl = if(basePath != null) {
                    Uri.build(
                        encodedPath = "${basePath}/${url.encodedPath.trim('/')}",
                        encodedQueryString = url.encodedQueryString,
                    )
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
