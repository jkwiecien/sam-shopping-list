package pl.techbrewery.sam.features.timber

import timber.log.Timber

class DebugLogsTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String? {
        return "SAM_LOG"
    }

}