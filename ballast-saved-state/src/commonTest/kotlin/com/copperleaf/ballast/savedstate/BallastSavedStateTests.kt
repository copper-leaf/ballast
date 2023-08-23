package com.copperleaf.ballast.savedstate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BallastSavedStateTests : StringSpec({
    "check toString values" {
        BallastSavedStateInterceptor<Any, Any, Any>(
            adapter = TestSavedStateAdapter()
        ).toString() shouldBe "BallastSavedStateInterceptor(adapter=TestSavedStateAdapter)"
    }
})

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
