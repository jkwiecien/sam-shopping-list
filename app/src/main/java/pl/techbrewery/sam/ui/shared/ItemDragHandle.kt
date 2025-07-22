package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Stable
val DragHandleSize = 24.dp

@Composable
fun ItemDragHandle() {
    Icon(
        imageVector = Icons.Default.Menu, // Or a drag handle icon
        contentDescription = "Category",
        modifier = Modifier.size(DragHandleSize)
    )
}