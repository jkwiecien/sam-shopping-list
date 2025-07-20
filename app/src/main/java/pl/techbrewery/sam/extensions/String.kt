package pl.techbrewery.sam.extensions

import androidx.compose.ui.text.capitalize
import java.util.Locale

fun String.capitalize(): String {
    return replaceFirstChar { it.uppercase() }
}