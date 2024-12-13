package com.copperleaf.ballast.sync

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BallastSyncTests {
    @Test
    fun checkToStringValues() = runTest {
        assertEquals<Any?>(
            "BallastSyncInterceptor(connection=DefaultSyncConnection(clientType=Source, adapter=InMemorySyncAdapter))",
            BallastSyncInterceptor<Any, Any, Any>(
                connection = DefaultSyncConnection(
                    clientType = DefaultSyncConnection.ClientType.Source,
                    adapter = InMemorySyncAdapter(),
                )
            ).toString()
        )
    }
}
