package pl.techbrewery.sam.ui.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissBackground(dismissDirection: SwipeToDismissBoxValue) {
    val color by animateColorAsState(
        when (dismissDirection) {
            SwipeToDismissBoxValue.EndToStart -> Red
            else -> Color.Transparent
        }
    )
    val alignment = when (dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }
    val scale by animateFloatAsState(
        if (dismissDirection != SwipeToDismissBoxValue.Settled) 1.25f else 0f
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                color = color,
                shape  = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = Spacing.Large),
        contentAlignment = alignment
    ) {
        Icon(
            Icons.Outlined.Delete,
            contentDescription = "Localized description",
            modifier = Modifier.scale(scale)
        )
    }
}