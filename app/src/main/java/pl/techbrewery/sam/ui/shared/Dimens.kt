package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimens {

}

object Spacing {
    @Stable
    val Tiny: Dp = 4.dp
    @Stable
    val Small: Dp = 8.dp
    @Stable
    val Medium: Dp = 12.dp
    @Stable
     val Large: Dp = 16.dp
    @Stable
    val XL: Dp = 24.dp
    @Stable
    val XXL: Dp = 32.dp
}

@Composable
fun TinySpacingBox() = Spacer(modifier = Modifier.size(Spacing.Tiny))

@Composable
fun SmallSpacingBox() = Spacer(modifier = Modifier.size(Spacing.Small))

@Composable
fun MediumSpacingBox() = Spacer(modifier = Modifier.size(Spacing.Medium))

@Composable
fun LargeSpacingBox() = Spacer(modifier = Modifier.size(Spacing.Large))

@Composable
fun XLSpacingBox() = Spacer(modifier = Modifier.size(Spacing.XL))

@Composable
fun XXLSpacingBox() = Spacer(modifier = Modifier.size(Spacing.XXL))