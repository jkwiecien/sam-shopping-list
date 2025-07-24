@file:OptIn(ExperimentalMaterial3Api::class)

package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.extensions.capitalize
import pl.techbrewery.sam.extensions.closeKeyboardOnPress
import pl.techbrewery.sam.features.stores.CreateStoreSheetContent
import pl.techbrewery.sam.features.stores.state.CreateStoreBottomSheetState
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.shared.BottomPageContentState
import pl.techbrewery.sam.shared.KeyboardDonePressed
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.ui.shared.DropdownItem
import pl.techbrewery.sam.ui.shared.ItemDragHandle
import pl.techbrewery.sam.ui.shared.PrimaryDropdown
import pl.techbrewery.sam.ui.shared.SearchField
import pl.techbrewery.sam.ui.shared.SharedModalBottomSheet
import pl.techbrewery.sam.ui.shared.SmallSpacingBox
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.theme.SAMTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    modifier: Modifier = Modifier,
    onListScrollChanged: (Boolean) -> Unit = {}
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQueryFLow.collectAsStateWithLifecycle()
    val onAction: (Any) -> Unit = { action ->
        when (action) {
            is ShoppingListScrollChanged -> onListScrollChanged(action.scrolled)
            else -> viewModel.onAction(action)
        }
    }

    ShoppingListScreenContent(
        items = items,
        searchQuery = searchQuery,
        bottomSheetContentState = viewModel.bottomSheetContentState,
        selectedStoreDropdownItem = viewModel.selectedStoreDropdownItem,
        storeDropdownItems = viewModel.storeDropdownItems.collectAsStateWithLifecycle().value,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
private fun ShoppingListScreenContent(
    items: ImmutableList<SingleItem>,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    selectedStoreDropdownItem: DropdownItem<Store>,
    storeDropdownItems: ImmutableList<DropdownItem<Store>>,
    bottomSheetContentState: BottomPageContentState? = null,
    onAction: (Any) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        val modalBottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ShoppingList(
            items = items,
            searchQuery = searchQuery,
            modifier = modifier,
            selectedStoreDropdownItem = selectedStoreDropdownItem,
            storeDropdownItems = storeDropdownItems,
            onAction = onAction
        )
        when (bottomSheetContentState) {
            is CreateStoreBottomSheetState -> CreateStoreModalBottomSheet(
                modalBottomSheetState,
                onAction
            )
        }
    }
}

@Composable
private fun CreateStoreModalBottomSheet(
    sheetState: SheetState,
    onAction: (Any) -> Unit = {}
) {
    SharedModalBottomSheet(
        sheetState = sheetState,
        onAction = onAction
    ) {
        CreateStoreSheetContent()
    }
}

@Composable
private fun ShoppingList(
    modifier: Modifier = Modifier,
    items: ImmutableList<SingleItem>,
    selectedStoreDropdownItem: DropdownItem<Store>,
    storeDropdownItems: ImmutableList<DropdownItem<Store>>,
    searchQuery: String = "",
    onAction: (Any) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .closeKeyboardOnPress(
                onPressedSomething = { focusManager.clearFocus() }
            )
            .padding(horizontal = Spacing.Large)
    ) {
        val lazyListState = rememberLazyListState()
        val reorderableLazyListState =
            rememberReorderableLazyListState(lazyListState) { from, to ->
                onAction(ItemMoved(from.index, to.index))
            }
        var hideDropdown by remember { mutableStateOf(false) }

        LaunchedEffect(lazyListState) {
            snapshotFlow { lazyListState.firstVisibleItemIndex }
                .collect { index ->
                    val listScrolled = index > 0
                    hideDropdown = listScrolled
                    onAction(ShoppingListScrollChanged(listScrolled))
                }
        }
        AnimatedVisibility(
            visible = !hideDropdown
        ) {
            Column {
                StoresDropdown(
                    selectedItem = selectedStoreDropdownItem,
                    items = storeDropdownItems,
                    onItemSelected = { onAction(StoreDropdownItemSelected(it)) }
                )
                SmallSpacingBox()
            }
        }
        SearchField(
            query = searchQuery,
            supportingText = "Add item",
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            onValueChange = { onAction(SearchQueryChanged(it)) },
            onDonePressed = { onAction(KeyboardDonePressed) },
            modifier = Modifier.fillMaxWidth()
        )
        SmallSpacingBox()
        LazyColumn(
            state = lazyListState
        ) {
            items(items, key = { it.itemName }) { item ->
                ReorderableItem(
                    reorderableLazyListState,
                    key = item.itemName
                ) { isDragging ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ItemDragHandle(
                            modifier = Modifier.draggableHandle(),
                        )
                        ShoppingListItem(
                            itemName = item.itemName,
                            onCheckboxChecked = { onAction(ItemChecked(it)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ShoppingListItem(
    itemName: String,
    modifier: Modifier = Modifier,
    onCheckboxChecked: (itemName: String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Small), // Increased padding for better touch target
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start // Align items to the start
    ) {
        Checkbox(
            checked = false,
            onCheckedChange = { checked ->
                if (checked) onCheckboxChecked(itemName)
            }
        )
        Text(
            text = itemName.capitalize(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StoresDropdown(
    items: ImmutableList<DropdownItem<Store>>,
    selectedItem: DropdownItem<Store>,
    onItemSelected: (DropdownItem<Store>) -> Unit = {}
) {
    PrimaryDropdown(
        selectedItem = selectedItem,
        items = items,
        onSelectedItemChanged = onItemSelected,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun ShoppingListScreenPreview() {
    SAMTheme {
        // 1. Create some dummy Store objects
        val dummyStores = listOf(
            Store(storeId = 1, name = "Main Store", main = true),
            Store(storeId = 2, name = "Second Store"),
            Store(storeId = 3, name = "Another Grocer")
        )

        // 2. Create DropdownItem<Store> objects for the dropdown
        val storeDropdownItems = dummyStores.map { store ->
            DropdownItem(item = store, text = store.name)
        }.toImmutableList()

        // 3. Select one as the default selected item
        val selectedStoreDropdownItem = storeDropdownItems.firstOrNull()
            ?: DropdownItem(
                item = Store.dummyStore(),
                text = "No Store"
            ) // Fallback if list is empty

        SAMTheme {
            ShoppingListScreenContent(
                items = listOf(
                    "apple", "Banana", "Milk", "Eggs", "Cheese", "Chicken", "Beef",
                    "Pork", "Salmon", "Tuna", "Pasta", "Rice", "Bread", "Cereal",
                    "Coffee", "Tea", "Juice", "Soda", "Water"
                ).map { SingleItem(it) }.toImmutableList(),
                searchQuery = "Preview Search", // Optional: Provide a preview search query
                selectedStoreDropdownItem = selectedStoreDropdownItem,
                storeDropdownItems = storeDropdownItems,
                // bottomSheetContentState can be null for this preview if not testing a sheet
                bottomSheetContentState = null,
                onAction = {} // Provide a no-op lambda for onAction
            )
        }
    }
}