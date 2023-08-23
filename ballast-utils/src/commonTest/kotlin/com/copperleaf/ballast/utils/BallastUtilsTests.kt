package com.copperleaf.ballast.utils

import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.KillSwitch
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BallastUtilsTests : StringSpec({
    "check toString values" {
        BootstrapInterceptor<Any, Any, Any>(getInitialInput = { Any() }).toString() shouldBe "BootstrapInterceptor"
        KillSwitch<Any, Any, Any>().toString() shouldBe "KillSwitch(gracePeriod=100ms)"
    }
})
