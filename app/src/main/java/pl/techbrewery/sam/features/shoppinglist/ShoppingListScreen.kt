@file:OptIn(ExperimentalMaterial3Api::class)

package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.extensions.capitalize
import pl.techbrewery.sam.features.stores.CreateStoreSheetContent
import pl.techbrewery.sam.features.stores.state.CreateStoreBottomSheetState
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.shared.BottomPageContentState
import pl.techbrewery.sam.shared.KeyboardDonePressed
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.ui.shared.ItemDragHandle
import pl.techbrewery.sam.ui.shared.PrimaryTextField
import pl.techbrewery.sam.ui.shared.SharedModalBottomSheet
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQueryFLow.collectAsStateWithLifecycle()
    val onAction: (Any) -> Unit = { viewModel.onAction(it) }

    BottomSheetAwareContent(
        items = items,
        searchQuery = searchQuery,
        bottomSheetContentState = viewModel.bottomSheetContentState,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
private fun BottomSheetAwareContent(
    items: ImmutableList<SingleItem>,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    bottomSheetContentState: BottomPageContentState? = null,
    onAction: (Any) -> Unit = {}
) {
    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ShoppingList(
        items = items,
        searchQuery = searchQuery,
        modifier = modifier,
        onAction = onAction
    )
    when (bottomSheetContentState) {
        is CreateStoreBottomSheetState -> CreateStoreModalBottomSheet(
            modalBottomSheetState,
            onAction
        )
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
    searchQuery: String = "",
    onAction: (Any) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        PrimaryTextField(
            value = searchQuery,
            supportingText = "Add item",
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            onValueChange = { onAction(SearchQueryChanged(it)) },
            onDonePressed = { onAction(KeyboardDonePressed) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(items, key = { it.itemName }) { item ->
                ShoppingListItem(
                    itemName = item.itemName,
                    onCheckboxChecked = { onAction(ItemChecked(it)) },
                    modifier = Modifier.animateItem()
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ShoppingListItem(
    itemName: String,
    onCheckboxChecked: (itemName: String) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // Increased padding for better touch target
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start // Align items to the start
    ) {
        ItemDragHandle()
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

@Preview(showBackground = true)
@Composable
private fun ShoppingListScreenPreview() {
    SAMTheme {
        ShoppingList(
            items = listOf(
                "apple", "Banana", "Milk", "Eggs", "Cheese", "Chicken", "Beef",
                "Pork", "Salmon", "Tuna", "Pasta", "Rice", "Bread", "Cereal",
                "Coffee", "Tea", "Juice", "Soda", "Water"
            ).map { SingleItem(it) }.toImmutableList()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShoppingListScreenCreateStorePreview() {
    SAMTheme {
        BottomSheetAwareContent(
            items = listOf(
                "apple", "Banana", "Milk", "Eggs", "Cheese", "Chicken", "Beef",
                "Pork", "Salmon", "Tuna", "Pasta", "Rice", "Bread", "Cereal",
                "Coffee", "Tea", "Juice", "Soda", "Water"
            ).map { SingleItem(it) }.toImmutableList(),
            bottomSheetContentState = CreateStoreBottomSheetState
        )
    }
}