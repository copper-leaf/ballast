package com.copperleaf.ballast.savedstate

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BallastSavedStateTests {
    @Test
    fun checkToStringValues() = runTest {
        assertEquals<Any?>(
            "BallastSavedStateInterceptor(adapter=TestSavedStateAdapter)", BallastSavedStateInterceptor<Any, Any, Any>(
                adapter = TestSavedStateAdapter()
            ).toString()
        )
    }
}

private class TestSavedStateAdapter : SavedStateAdapter<Any, Any, Any> {
    override suspend fun SaveStateScope<Any, Any, Any>.save() {
        TODO("Not yet implemented")
    }

    override suspend fun RestoreStateScope<Any, Any, Any>.restore(): Any {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "TestSavedStateAdapter"
    }
}
