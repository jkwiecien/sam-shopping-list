package pl.techbrewery.sam.kmp.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun getCurrentTime(): String {
    return Clock.System.now().toString()
}