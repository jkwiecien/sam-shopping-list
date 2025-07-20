package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.shared.BottomSheetDismissRequested

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedModalBottomSheet(
    sheetState: SheetState,
    onAction: (Any) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(BottomSheetDismissRequested) },
        dragHandle = {  },
        sheetState = sheetState,
        containerColor = Color.Companion.White,
        content = {
            Box(
                contentAlignment = Alignment.TopCenter
            ) {
                content()
                DragHandle(
                    modifier = Modifier.width(50.dp),
                    color = Color.LightGray
                )
            }
        }
    )
}