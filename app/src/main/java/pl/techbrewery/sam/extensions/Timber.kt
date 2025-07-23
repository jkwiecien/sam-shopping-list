package pl.techbrewery.sam.extensions

import timber.log.Timber

fun tempLog(message: String) {
    // This is a placeholder for the logging function.
    // Replace with your actual logging implementation.
    Timber.tag("sam-temp").d(message)
}