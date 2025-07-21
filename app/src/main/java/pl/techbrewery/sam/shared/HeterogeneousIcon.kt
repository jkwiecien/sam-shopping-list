package pl.techbrewery.sam.shared

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

sealed interface HeterogeneousVectorIcon {
    data class VectorIcon(val imageVector: ImageVector) : HeterogeneousVectorIcon
    data class PainterIcon(@DrawableRes val resId: Int) : HeterogeneousVectorIcon
}

@Composable
fun HeterogeneousIcon(
    icon: HeterogeneousVectorIcon,
    contentDescription: String? = null,
    tint: Color = Black,
    modifier: Modifier = Modifier
) {
    when (icon) {
        is HeterogeneousVectorIcon.VectorIcon -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                tint = tint,
                modifier = modifier
            )
        }
        is HeterogeneousVectorIcon.PainterIcon -> {
          Icon(
                painter = painterResource(id = icon.resId),
                contentDescription = contentDescription,
                tint = tint,
                modifier = modifier
            )
        }
    }
}