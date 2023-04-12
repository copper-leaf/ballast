package com.copperleaf.ballast.navigation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

object Assertions {
    fun <T> assertEquals(expected: T?, actual: T?) {
        actual shouldBe expected
    }

    fun <T> assertSame(expected: T?, actual: T?) {
        actual shouldBeSameInstanceAs expected
    }

    fun assertFalse(actual: Boolean?) {
        actual shouldBe false
    }

    fun assertTrue(block: ()->Boolean?) {
        block() shouldBe true
    }

    fun assertNull(actual: Any?) {
        actual shouldBe null
    }

    fun assertFails(block: () -> Unit) {
        shouldThrow<Throwable>(block)
    }

    inline fun <reified T : Throwable> assertFailsWith(block: () -> Unit): T {
        return shouldThrow<T>(block)
    }
}
