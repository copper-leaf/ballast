package com.copperleaf.ballast.examples.bgg.models

/**
 * An item from the BGG XML API v2
 *
 * Example Request:
 *     https://boardgamegeek.com/xmlapi2/hot?type=boardgame
 *     http://localhost:8080/proxy/bgg/hot?type=boardgame
 * Example response:
 *     <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
 *       <item id="343362" rank="1">
 *         <thumbnail value="https://cf.geekdo-images.com/skiu7wU44_3H2pBjstUB9A__thumb/img/bDkcFjxXbtNkGE3VlxBqYUyYvkU=/fit-in/200x150/filters:strip_icc()/pic6739647.jpg"/>
 *         <name value="Oak"/>
 *         <yearpublished value="2022"/>
 *       </item>
 *     </items>
 */
data class BggHotListItem(
    val id: Long = 0L,
    val rank: Int = 0,
    val thumbnail: String = "",
    val name: String = "",
    val yearPublished: Int? = null,
)
