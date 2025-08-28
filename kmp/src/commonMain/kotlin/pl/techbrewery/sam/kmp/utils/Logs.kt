package pl.techbrewery.sam.kmp.utils

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier

fun tempLog(message: String) {
    Napier.d(
        message = message,
        tag = "sam-temp"
    )
}

fun debugLog(message: String, tag: String) {
    Napier.d(
        message = message,
        tag = tag
    )
}

fun warningLog(message: String, tag: String, error: Exception? = null) {
    Napier.w(
        message = message,
        tag = tag,
        throwable = error
    )
}

fun errorLog(message: String, tag: String, error: Exception? = null) {
    Napier.e(
        message = message,
        tag = tag,
        throwable = error
    )
}

class SAMAntilog(
    private val debugAntilog: DebugAntilog = DebugAntilog("SAM")
) : Antilog() {

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val prefixedTag = if (tag.isNullOrEmpty()) "SAM" else "SAM-$tag"
        debugAntilog.log(priority, prefixedTag, throwable, message)
    }
}
