@file:OptIn(ExperimentalContracts::class)

package com.copperleaf.ballast.analytics

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

fun Any?.shouldBeNull() {
    contract {
        returns() implies (this@shouldBeNull == null)
    }

    assertNull(this)
}

fun <T> T?.shouldNotBeNull(): T {
    contract {
        returns() implies (this@shouldNotBeNull != null)
    }

    return assertNotNull(this)
}

inline infix fun Any?.shouldBe(other: Any?) {
    assertEquals(other, this)
}

inline fun <T> List<T>.shouldBeEmpty() {
    assertEquals(0, this.size)
}

inline fun <reified T> Any?.shouldBeInstanceOf() {
    assertTrue(this is T)
}
