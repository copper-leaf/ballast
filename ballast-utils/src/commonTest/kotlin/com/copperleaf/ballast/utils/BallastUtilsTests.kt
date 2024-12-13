package com.copperleaf.ballast.utils

import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.KillSwitch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BallastUtilsTests {
    @Test
    fun checkToStringValues() = runTest {
        assertEquals<Any?>(
            "BootstrapInterceptor",
            BootstrapInterceptor<Any, Any, Any>(getInitialInput = { Any() }).toString()
        )
        assertEquals<Any?>("KillSwitch(gracePeriod=100ms)", KillSwitch<Any, Any, Any>().toString())
    }
}
