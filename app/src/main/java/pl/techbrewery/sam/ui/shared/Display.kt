package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun statusBarHeight(): Dp {
    val density = LocalDensity.current
    val insets = WindowInsets.statusBars.getTop(LocalDensity.current)
    return with(density) { insets.toDp() }
}

@Composable
fun statusBarAsTopPadding(): Dp {
    return WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
}