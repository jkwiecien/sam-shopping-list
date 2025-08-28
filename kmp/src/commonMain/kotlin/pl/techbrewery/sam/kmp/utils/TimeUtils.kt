package pl.techbrewery.sam.kmp.utils

import dev.gitlive.firebase.firestore.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun getCurrentTime(): String {
    return Clock.System.now().toString()
}

const val DATE_TIME_FORMATTER_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"

fun dateTimeFormatter(): DateTimeFormatter = DateTimeFormatter
    .ofPattern(DATE_TIME_FORMATTER_PATTERN)
    .withZone(ZoneId.of("UTC"))

object TimeUtils {
     fun timestampToString(timestamp: Timestamp?): String {
        timestamp ?: return ""
        val instant = Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong())
        val formatter = dateTimeFormatter()
        return formatter.format(instant)
    }

     fun timestampFromString(dateString: String): Timestamp {
        val localDateTime = LocalDateTime.parse(dateString, dateTimeFormatter())
        val seconds = localDateTime.atZone(ZoneId.of("UTC")).toEpochSecond()
        return Timestamp(seconds, 0)
    }
}