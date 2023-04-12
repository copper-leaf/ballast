package com.copperleaf.ballast.examples.api.models

/**
 * An item from the BGG XML API v2 /hot
 *
 * Example response:
 * <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
 *   <item id="343362" rank="1">
 *     <thumbnail value="https://cf.geekdo-images.com/skiu7wU44_3H2pBjstUB9A__thumb/img/bDkcFjxXbtNkGE3VlxBqYUyYvkU=/fit-in/200x150/filters:strip_icc()/pic6739647.jpg"/>
 *     <name value="Oak"/>
 *     <yearpublished value="2022"/>
 *   </item>
 * </items>
 */
enum class HotListType(val value: String, val displayName: String) {
    BoardGame("boardgame", "Board Game"),
    Rpg("rpg", "RPG"),
    VideoGame("videogame", "Video Game"),

    BoardGamePerson("boardgameperson", "Board Game Person"),
    RpgPerson("rpgperson", "RPG Person"),

    BoardGameCompany("boardgamecompany", "Board Game Company"),
    RpgCompany("rpgcompany", "RPG Company"),
    VideoGameCompany("videogamecompany", "Video Game Company"),
}
