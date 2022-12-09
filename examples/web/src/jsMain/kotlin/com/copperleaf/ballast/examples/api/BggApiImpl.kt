package com.copperleaf.ballast.examples.api

import com.copperleaf.ballast.examples.api.models.BggHotListItem
import com.copperleaf.ballast.examples.api.models.HotListType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.DEFAULT_PORT
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.w3c.dom.Element
import org.w3c.dom.parsing.DOMParser

class BggApiImpl(
    private val httpClient: HttpClient
) : BggApi {
    override suspend fun getHotGames(type: HotListType): List<BggHotListItem> {
        // adda short delay because the API call is so quick, to make sure the user can see when the fetch is happening
        delay(1500)

        val currentPath = window.location.pathname.trim('/')

        val basePath = when {
            currentPath.startsWith("ballast/wiki") -> {
                // when running in the prod Orchid docs site, the cached APIs are at a different path...
                "/ballast/assets/example/distributions/"
            }
            currentPath.startsWith("wiki/") -> {
                // when running in the local Orchid docs site, the cached APIs are at a different path...
                "/assets/example/distributions/"
            }
            else -> {
                "/"
            }
        }

        val (currentHost: String, currentPort: Int) = if(window.location.host.contains(":")) {
            window.location.host.split(":")[0] to (window.location.host.split(":")[1].toIntOrNull() ?: DEFAULT_PORT)
        } else {
            window.location.host to DEFAULT_PORT
        }

        val response: HttpResponse = httpClient.get {
            url(
                scheme = window.location.protocol.trimEnd(':'),
                host = currentHost,
                port = currentPort,
                path = "${basePath}bgg/hot/${type.value}.xml",
            )
        }
        val stringBody: String = response.bodyAsText()

        val parser = DOMParser()
        val doc = parser.parseFromString(stringBody, "application/xml");
        val itemNodes = doc.querySelectorAll("item")

        return buildList {
            for(nodeIndex in 0 until itemNodes.length) {
                val node = itemNodes.item(nodeIndex) as? Element ?: continue

                this += BggHotListItem(
                    id = node.getAttribute("id")?.toLongOrNull() ?: 0L,
                    rank = node.getAttribute("rank")?.toIntOrNull() ?: 0,
                    name = node.querySelector("name")?.getAttribute("value") ?: "",
                    thumbnail = node.querySelector("thumbnail")?.getAttribute("value") ?: "",
                    yearPublished = node.querySelector("yearpublished")?.getAttribute("value")?.toIntOrNull(),
                )
            }
        }
    }
}
