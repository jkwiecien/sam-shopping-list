package pl.techbrewery.sam.kmp.utils

import io.github.aakira.napier.Napier

fun tempLog(message: String) {
    Napier.d(
        message = message,
        tag = "sam-temp"
    )
}