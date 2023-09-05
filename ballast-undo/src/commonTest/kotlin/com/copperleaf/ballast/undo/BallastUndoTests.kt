package com.copperleaf.ballast.undo

import com.copperleaf.ballast.undo.state.StateBasedUndoController
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BallastUndoTests : StringSpec({
    "check toString values" {
        BallastUndoInterceptor<Any, Any, Any>(
            controller = StateBasedUndoController()
        ).toString() shouldBe "BallastUndoInterceptor(controller=StateBasedUndoController)"
    }
})
