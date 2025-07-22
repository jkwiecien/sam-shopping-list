package pl.techbrewery.sam.extensions

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun Modifier.closeKeyboardOnPress(
    onPressedSomething: () -> Unit = {}
): Modifier {
    val controller = LocalSoftwareKeyboardController.current
    return this.pointerInput(Unit) {
        detectTapGestures(onPress = {
            controller?.hide()
            onPressedSomething()
        })
    }
}