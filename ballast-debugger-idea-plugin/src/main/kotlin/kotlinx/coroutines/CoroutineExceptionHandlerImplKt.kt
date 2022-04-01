package kotlinx.coroutines

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

// A hack to fix errors like 'java.lang.NoClassDefFoundError: Could not initialize class kotlinx.coroutines.CoroutineExceptionHandlerImplKt'
// I'm assuming that's because Ballast is compiled with Coroutine 1.6.0, but we had to downgrade to 1.5.2 to run it in
// the IDEA plugin at all, and this must be a new class from 1.6.0.
class CoroutineExceptionHandlerImpl : AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // ignore
    }

    fun handleCoroutineException(context: CoroutineContext, exception: Throwable) {
        // ignore
    }
}
