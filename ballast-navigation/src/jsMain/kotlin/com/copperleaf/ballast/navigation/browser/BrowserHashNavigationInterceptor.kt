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
import org.w3c.dom.HashChangeEvent

public class BrowserHashNavigationInterceptor<T : Route>(
    initialRoute: T,
) : BaseBrowserNavigationInterceptor<T>(initialRoute) {

    override fun getInitialUrl(): Url? {
        val hashValue = window.location.hash.trim().trimStart('#').trimStart('/')
        val hashPieces = hashValue.split('?')

        val (initialPath: String?, initialQueryString: String?) = if(hashPieces.size == 2) {
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
            URLBuilder()
                .apply {
                    encodedPath = "/$initialPath"
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
        return callbackFlow<Url> {
            window.onhashchange = { event: HashChangeEvent ->
                this@callbackFlow.trySend(Url(event.newURL.split("#").last()))
                Unit
            }

            awaitClose {
                window.onhashchange = null
            }
        }
    }

    override fun setDestinationUrl(url: Url) {
        window.location.hash = url.encodedPathAndQuery
    }
}
