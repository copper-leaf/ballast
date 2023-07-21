package com.copperleaf.ballast.contracts.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

public class NamedDispatcher(
    public val name: String,
    private val delegate: CoroutineDispatcher
) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        return delegate.dispatch(context, block)
    }

    override fun toString(): String {
        return "NamedDispatcher(name='$name', delegate=$delegate)"
    }
}
