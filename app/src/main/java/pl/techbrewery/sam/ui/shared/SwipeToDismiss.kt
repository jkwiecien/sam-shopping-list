package pl.techbrewery.sam.ui.shared

import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable

@Composable
fun rememberSwipeToDeleteBoxState(
    onConfirmReturn: Boolean  = true,
    onDeleteConfirmed: () -> Unit
): SwipeToDismissBoxState {
    return rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDeleteConfirmed()
            }
            onConfirmReturn
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.75f }
    )
}