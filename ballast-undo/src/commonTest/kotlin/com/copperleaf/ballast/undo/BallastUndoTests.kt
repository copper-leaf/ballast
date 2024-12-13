package com.copperleaf.ballast.undo

import com.copperleaf.ballast.undo.state.StateBasedUndoController
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BallastUndoTests {
    @Test
    fun checkToStringValues() = runTest {
        assertEquals<Any?>(
            "BallastUndoInterceptor(controller=StateBasedUndoController)", BallastUndoInterceptor<Any, Any, Any>(
                controller = StateBasedUndoController()
            ).toString()
        )
    }
}
