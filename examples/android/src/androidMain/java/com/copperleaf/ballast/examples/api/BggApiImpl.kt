package com.copperleaf.ballast.examples.api

import com.copperleaf.ballast.examples.api.models.BggHotListItem
import com.copperleaf.ballast.examples.api.models.HotListType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.delay
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class BggApiImpl(
    private val httpClient: HttpClient
) : BggApi {
    override suspend fun getHotGames(type: HotListType): List<BggHotListItem> {
        // adda short delay because the API call is so quick, to make sure the user can see when the fetch is happening
        delay(1500)

        val response: HttpResponse = httpClient.get(urlString = "https://boardgamegeek.com/xmlapi2/hot?type=${type.value}")

        val builderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder: DocumentBuilder = builderFactory.newDocumentBuilder()
        val doc: Document = docBuilder.parse(ByteArrayInputStream(response.readBytes()))

        val itemNodes = doc.getElementsByTagName("item")

        return buildList {
            for(nodeIndex in 0 until itemNodes.length) {
                val node = itemNodes.item(nodeIndex) as? Element ?: continue

                this += BggHotListItem(
                    id = node.getAttribute("id")?.toLongOrNull() ?: 0L,
                    rank = node.getAttribute("rank")?.toIntOrNull() ?: 0,
                    name = (node.getElementsByTagName("name")?.item(0) as? Element)?.getAttribute("value") ?: "",
                    thumbnail = (node.getElementsByTagName("thumbnail")?.item(0) as? Element)?.getAttribute("value") ?: "",
                    yearPublished = (node.getElementsByTagName("yearpublished")?.item(0) as? Element)?.getAttribute("value")?.toIntOrNull(),
                )
            }
        }
    }
}
