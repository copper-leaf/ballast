@file:Suppress("UNCHECKED_CAST")

package com.copperleaf.ballast.ktor

import com.copperleaf.ballast.BallastViewModel
import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey

public inline fun <
        reified VM : BallastViewModel<Inputs, Events, State>,
        reified Inputs : Any,
        reified Events : Any,
        reified State : Any
        > ApplicationCall.ballastViewModel(
    key: AttributeKey<VM>
): VM {
    return application.attributes[key]
}
