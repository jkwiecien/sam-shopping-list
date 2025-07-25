@file:OptIn(ExperimentalMaterial3Api::class)

package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.extensions.capitalize
import pl.techbrewery.sam.extensions.closeKeyboardOnPress
import pl.techbrewery.sam.features.shoppinglist.ui.ItemTextField
import pl.techbrewery.sam.features.shoppinglist.ui.StoresDropdown
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.ui.shared.DropdownItem
import pl.techbrewery.sam.ui.shared.ItemDragHandle
import pl.techbrewery.sam.ui.shared.ScrollListener
import pl.techbrewery.sam.ui.shared.SmallSpacingBox
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.SwipeToDismissBackground
import pl.techbrewery.sam.ui.theme.SAMTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    modifier: Modifier = Modifier,
    onStoreDropdownVisibilityChanged: (visible: Boolean) -> Unit = {}
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQueryFlow.collectAsStateWithLifecycle()
    val onAction: (Any) -> Unit = { action ->
        when (action) {
            is StoresDropdownVisibilityChanged -> onStoreDropdownVisibilityChanged(action.visible)
            else -> viewModel.onAction(action)
        }
    }

    ShoppingListScreenContent(
        items = items,
        storeDropdownItems = viewModel.storeDropdownItems.collectAsStateWithLifecycle().value,
        suggestedItems = viewModel.suggestedItemsDropdownItems.collectAsStateWithLifecycle().value,
        selectedStoreDropdownItem = viewModel.selectedStoreDropdownItemFlow.collectAsStateWithLifecycle().value,
        searchQuery = searchQuery,
        itemTextFieldError = viewModel.itemTextFieldError,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
private fun ShoppingListScreenContent(
    modifier: Modifier = Modifier,
    items: ImmutableList<SingleItem>,
    suggestedItems: ImmutableList<DropdownItem<SingleItem>>,
    selectedStoreDropdownItem: DropdownItem<Store>,
    storeDropdownItems: ImmutableList<DropdownItem<Store>>,
    searchQuery: String = "",
    itemTextFieldError: String? = null,
    onAction: (Any) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    Surface {
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

            val hasMultipleStores = storeDropdownItems.size > 1
            var scrolledUp by remember { mutableStateOf(true) }
            var showStoresDropdown by remember(hasMultipleStores && scrolledUp) {
                mutableStateOf(
                    hasMultipleStores && scrolledUp
                )
            }

            LaunchedEffect(showStoresDropdown) {
                onAction(StoresDropdownVisibilityChanged(showStoresDropdown))
            }

            ScrollListener(
                listState = lazyListState,
                onScrolledUp = { scrolledUp = true },
                onScrolledDown = { scrolledUp = false }
            )

            AnimatedVisibility(
                visible = showStoresDropdown
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
            ItemTextField(
                value = searchQuery,
                expanded = suggestedItems.isNotEmpty(),
                suggestedItems = suggestedItems,
                itemTextFieldError = itemTextFieldError,
                onValueChange = { onAction(SearchQueryChanged(it)) },
                onDonePressed = { onAction(ItemFieldKeyboardDonePressed) },
                onSelectedItemChanged = { onAction(SuggestedItemSelected(it)) },
                modifier = Modifier.fillMaxWidth()
            )
            SmallSpacingBox()
            LazyColumn(
                state = lazyListState
            ) {
                items(items, key = { it.itemId }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) onAction(
                                ShoppingListItemDismissed(item)
                            )
                            true
                        },
                        positionalThreshold = { totalDistance -> totalDistance * 0.75f }
                    )

                    ReorderableItem(
                        reorderableLazyListState,
                        key = item.itemId
                    ) { isDragging ->
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                SwipeToDismissBackground(dismissState.dismissDirection)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ItemDragHandle(
                                    modifier = Modifier.draggableHandle(),
                                )

                                ShoppingListItem(
                                    itemName = item.itemName,
                                    onCheckboxChecked = { onAction(ItemChecked(item.itemId)) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ShoppingListItem(
    itemName: String,
    modifier: Modifier = Modifier,
    onCheckboxChecked: () -> Unit
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
                if (checked) onCheckboxChecked()
            }
        )
        Text(
            text = itemName.capitalize(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShoppingListScreenPreview() {
    SAMTheme {
        // 1. Create some dummy Store objects
        val dummyStores = listOf(
            Store(storeId = 1, name = "Main Store", selected = true),
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
                item = Store.createInitialStore(),
                text = "No Store"
            ) // Fallback if list is empty

        SAMTheme {
            ShoppingListScreenContent(
                items = listOf(
                    "apple", "Banana", "Milk", "Eggs", "Cheese", "Chicken", "Beef",
                    "Pork", "Salmon", "Tuna", "Pasta", "Rice", "Bread", "Cereal",
                    "Coffee", "Tea", "Juice", "Soda", "Water"
                ).map { SingleItem(itemName = it, storeId = 0) }.toImmutableList(),
                suggestedItems = emptyList<DropdownItem<SingleItem>>().toImmutableList(),
                searchQuery = "Preview Search", // Optional: Provide a preview search query
                selectedStoreDropdownItem = selectedStoreDropdownItem,
                storeDropdownItems = storeDropdownItems,
                // bottomSheetContentState can be null for this preview if not testing a sheet
                onAction = {} // Provide a no-op lambda for onAction
            )
        }
    }
}