package com.copperleaf.ballast.navigation.browser

import com.copperleaf.ballast.navigation.internal.Uri
import com.copperleaf.ballast.navigation.internal.UriBuilder
import com.copperleaf.ballast.navigation.routing.Route
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.w3c.dom.HashChangeEvent

public class BrowserHashNavigationInterceptor<T : Route>(
    initialRoute: T,
) : BaseBrowserNavigationInterceptor<T>(initialRoute) {

    override fun getInitialUrl(): Uri? {
        val hashValue = window.location.hash.trim().trimStart('#').trimStart('/')
        val hashPieces = hashValue.split('?')

        val (initialPath: String?, initialQueryString: String?) = if (hashPieces.size == 2) {
            // we have path and query in the hash value
            val path = hashPieces[0].takeIf { it.isNotBlank() }
            val query = hashPieces[1].takeIf { it.isNotBlank() }
            path to query
        } else {
            // only have the path in the hash value
            val path = hashPieces[0].takeIf { it.isNotBlank() }
            val query = window.location.search.trimStart('?').takeIf { it.isNotBlank() }
            path to query
        }

        return if (!initialPath.isNullOrBlank() || !initialQueryString.isNullOrBlank()) {
            UriBuilder.build(
                encodedPath = "/$initialPath".also { println("initialPath: $it") },
                encodedQueryString = initialQueryString,
            )
        } else {
            null
        }
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun watchForUrlChanges(): Flow<Uri> {
        return callbackFlow<Uri> {
            window.onhashchange = { event: HashChangeEvent ->
                val partAfterHash = event.newURL?.split("#")?.last()
                if (!partAfterHash.isNullOrBlank()) {
                    this@callbackFlow.trySend(UriBuilder.parse(partAfterHash))
                }
                Unit
            }

            awaitClose {
                window.onhashchange = null
            }
        }
    }

    override fun setDestinationUrl(url: Uri) {
        window.location.hash = url.encodedPathAndQuery
    }
}
