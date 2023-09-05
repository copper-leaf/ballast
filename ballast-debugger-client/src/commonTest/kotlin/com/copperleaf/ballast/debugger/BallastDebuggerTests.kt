package com.copperleaf.ballast.debugger

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

class BallastDebuggerTests : StringSpec({
    "check toString values" {
        BallastDebuggerInterceptor<Any, Any, Any>(
            connection = BallastDebuggerClientConnection(TestHttpEngine, this),
        ).toString() shouldBe "BallastDebuggerInterceptor"
    }
})

private object TestHttpEngine : HttpClientEngineFactory<TestEngineConfig> {
    override fun create(block: TestEngineConfig.() -> Unit): HttpClientEngine {
        return TestHttpClientEngine()
    }

    override fun toString(): String = "TestHttpEngine"
}

private class TestEngineConfig : HttpClientEngineConfig()
private class TestHttpClientEngine : HttpClientEngine {
    override val config: HttpClientEngineConfig
        get() = TODO("Not yet implemented")
    override val dispatcher: CoroutineDispatcher
        get() = TODO("Not yet implemented")

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
}
