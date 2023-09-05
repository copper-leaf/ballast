package com.copperleaf.ballast.sync

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BallastSyncTests : StringSpec({
    "check toString values" {
        BallastSyncInterceptor<Any, Any, Any>(
            connection = DefaultSyncConnection(
                clientType = DefaultSyncConnection.ClientType.Source,
                adapter = InMemorySyncAdapter(),
            )
        ).toString() shouldBe "BallastSyncInterceptor(connection=DefaultSyncConnection(clientType=Source, adapter=InMemorySyncAdapter))"
    }
})
