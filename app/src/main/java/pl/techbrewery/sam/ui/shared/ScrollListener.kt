package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import pl.techbrewery.sam.kmp.utils.tempLog

enum class LastScrollDirection {
    UP, DOWN, NONE
}

@Composable
fun ScrollListener(
    listState: LazyListState,
    onScrolled: (LastScrollDirection) -> Unit = {},
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
            .filter { (_, currentOffset) -> currentOffset > threshold }
            .collect { (currentIndex, currentOffset) ->
                // Check if the scroll is significant enough to act on
                if (listState.isScrollInProgress) {
                    if (currentIndex > previousIndex) {
                        onScrolled(LastScrollDirection.UP)
                    } else if (currentIndex < previousIndex) {
                        onScrolled(LastScrollDirection.DOWN)
                    } else {
                        if (currentOffset > previousOffset) {
                            onScrolled(LastScrollDirection.UP)
                        } else if (currentOffset < previousOffset) {
                            onScrolled(LastScrollDirection.DOWN)
                        }
                    }
                }
                previousIndex = currentIndex
                previousOffset = currentOffset
            }
    }
}