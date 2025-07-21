package pl.techbrewery.sam.ui.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun ItemDragHandle() {
    Icon(
        imageVector = Icons.Default.Menu, // Or a drag handle icon
        contentDescription = "Category"
    )
}