package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@Composable
fun ScrollListener(
    listState: LazyListState,
    onScrolledUp: () -> Unit = {},
    onScrolledDown: () -> Unit = {},
    threshold: Int = 120
) {
    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow {
            // Create a pair of the current index and offset
            Pair(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            )
        }
            .debounce { 10L }
            .filter { (_, currentOffset) -> currentOffset > threshold }
            .collect { (currentIndex, currentOffset) ->
                // Check if the scroll is significant enough to act on
                if (listState.isScrollInProgress) {
                    if (currentIndex > previousIndex) {
                        onScrolledDown()
                    } else if (currentIndex < previousIndex) {
                        onScrolledUp()
                    } else {
                        if (currentOffset > previousOffset) {
                            onScrolledDown()
                        } else if (currentOffset < previousOffset) {
                            onScrolledUp()
                        }
                    }
                }
                previousIndex = currentIndex
                previousOffset = currentOffset
            }
    }
}