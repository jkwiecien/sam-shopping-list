package pl.techbrewery.sam.shared

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

@Suppress("FunctionName")
internal inline fun DefaultExceptionHandler(
    coroutineScope: CoroutineScope,
    crossinline handler: (CoroutineContext, Throwable) -> Unit
): CoroutineExceptionHandler =
    object : AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
        override fun handleException(context: CoroutineContext, exception: Throwable) {
            coroutineScope.launch(Dispatchers.Main) {
                handler.invoke(context, exception)
            }
        }
    }