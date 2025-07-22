package pl.techbrewery.sam.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun stringResourceCompat(resource: StringResource, previewFallback: String): String {
    return if (LocalInspectionMode.current) {
        // We are in a @Preview composable
        previewFallback
    } else {
        // We are in a real runtime environment
        stringResource(resource)
    }
}