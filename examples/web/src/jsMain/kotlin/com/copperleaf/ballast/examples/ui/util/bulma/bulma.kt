package com.copperleaf.ballast.examples.ui.util.bulma

import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.Element

enum class BulmaColor(val cssClass: String) {
    Default(""),

    White("is-white"),
    Light("is-light"),
    Dark("is-dark"),
    Black("is-black"),
    Text("is-text"),
    Ghost("is-ghost"),

    Primary("is-primary"),
    Link("is-link"),
    Info("is-info"),
    Success("is-success"),
    Warning("is-warning"),
    Danger("is-danger"),
}

enum class BulmaSize(val cssClass: String) {
    Small("is-small"),
    Default(""),
    Normal("is-normal"),
    Medium("is-medium"),
    Large("is-large"),
}

fun <T: Element> AttrsScope<T>.ClassList(
    block: MutableSet<String>.() -> Unit,
) {
    classes(*buildSet<String> { block() }.toTypedArray())
}
