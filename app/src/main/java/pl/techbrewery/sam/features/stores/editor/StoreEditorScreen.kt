package pl.techbrewery.sam.features.stores.editor

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.techbrewery.sam.extensions.closeKeyboardOnPress
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.action_save
import pl.techbrewery.sam.shared.rememberDragAndDropListState
import pl.techbrewery.sam.ui.shared.DragHandleSize
import pl.techbrewery.sam.ui.shared.ItemDragHandle
import pl.techbrewery.sam.ui.shared.PrimaryFilledButton
import pl.techbrewery.sam.ui.shared.PrimaryTextField
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.stringResourceCompat
import pl.techbrewery.sam.ui.theme.SAMTheme
import timber.log.Timber

@Composable
fun StoreEditorScreen(
    viewModel: StoreEditorViewModel,
) {
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    StoreEditorScreenContent(
        storeName = viewModel.storeName,
        newDepartmentName = viewModel.newDepartmentName,
        departments = departments,
        onAction = { viewModel.onAction(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreEditorScreenContent(
    storeName: String,
    newDepartmentName: String,
    modifier: Modifier = Modifier,
    departments: ImmutableList<StoreDepartment> = emptyList<StoreDepartment>().toImmutableList(),
    onAction: (Any) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        val focusManager = LocalFocusManager.current
        Column(
            modifier = modifier
                .padding(Spacing.Large)
                .closeKeyboardOnPress(
                    onPressedSomething = { focusManager.clearFocus() }
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large)
        ) {
            PrimaryTextField(
                value = storeName,
                supportingText = "Store name",
                onValueChange = { onAction(StoreNameChanged(it)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                onDonePressed = { onAction(KeyboardDonePressedOnStoreName) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Customize the order of categories to match your shopping path in the store. Drag and drop categories to reflect your route.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Departments",
                style = MaterialTheme.typography.titleMedium
            )
            PrimaryTextField(
                value = newDepartmentName,
                supportingText = "Add department",
                onValueChange = { onAction(DepartmentNameChanged(it)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                onDonePressed = { onAction(KeyboardDonePressedOnDepartmentName) },
                modifier = Modifier.fillMaxWidth()
            )

            val lazyListState = rememberLazyListState()
            val dragAndDropListState =
                rememberDragAndDropListState(lazyListState) { from, to ->
                    onAction(StoreDepartmentMoved(from, to))
                }
            val coroutineScope = rememberCoroutineScope()
            var overscrollJob by remember { mutableStateOf<Job?>(null) }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDrag = { change, offset ->
                                change.consume()
                                dragAndDropListState.onDrag(offset)

                                if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress

                                dragAndDropListState
                                    .checkOverscroll()
                                    .takeIf { it != 0f }
                                    ?.let {
                                        overscrollJob = coroutineScope.launch {
                                            dragAndDropListState.lazyListState.scrollBy(it)
                                        }
                                    } ?: kotlin.run { overscrollJob?.cancel() }
                            },
                            onDragStart = { offset ->
                                val x = offset.x
                                val maxX = (DragHandleSize + Spacing.Small).toPx().toInt()
                                if (x >= 0 && x <= maxX) {
                                    dragAndDropListState.onDragStart(offset)
                                }
                            },
                            onDragEnd = {
                                dragAndDropListState.onDragInterrupted()
                            },
                            onDragCancel = {
                                dragAndDropListState.onDragInterrupted()
                            }
                        )
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    departments,
                    key = { index, department -> department.departmentName }) { index, department ->
                    CategoryItem(
                        categoryName = department.departmentName,
                        modifier = Modifier
                            .animateItem()
                            .composed {
                                val offsetOrNull =
                                    dragAndDropListState.elementDisplacement.takeIf {
                                        index == dragAndDropListState.currentIndexOfDraggedItem
                                    }
                                Modifier.graphicsLayer {
                                    translationY = offsetOrNull ?: 0f
                                }
                            }
                    )
                }
            }
            PrimaryFilledButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResourceCompat(Res.string.action_save, "Save"),
                onPressed = { onAction(SaveStorePressed) }
            )
        }
    }
}

@Composable
fun CategoryItem(
    categoryName: String,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ItemDragHandle()
        Text(text = categoryName, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun StoresScreenPreview() {
    SAMTheme { // Wrap with your app's theme if you have one
        StoreEditorScreenContent(
            storeName = "Lidl",
            newDepartmentName = "Vegetables"
        )
    }
}