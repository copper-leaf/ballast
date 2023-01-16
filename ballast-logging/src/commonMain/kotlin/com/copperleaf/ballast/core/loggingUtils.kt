package com.copperleaf.ballast.core

public fun formatMessage(tag: String?, message: String): String {
    return if(tag != null) {
        "[$tag] $message"
    } else {
        message
    }
}
