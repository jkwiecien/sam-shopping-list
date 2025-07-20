package pl.techbrewery.sam.features.shoppinglist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.techbrewery.sam.extensions.capitalize
import pl.techbrewery.sam.features.shoppinglist.state.ShoppingListItemsState
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.shared.KeyboardDonePressed
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.ui.theme.SAMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    val itemsState by viewModel.itemsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQueryFLow.collectAsStateWithLifecycle()
    ShoppingList(
        itemsState = itemsState,
        searchQuery = searchQuery,
        modifier = Modifier.padding(paddingValues),
        onAction = { viewModel.onAction(it) }
    )
}

@Composable
private fun ShoppingList(
    modifier: Modifier = Modifier,
    itemsState: ShoppingListItemsState,
    searchQuery: String = "",
    onAction: (Any) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search/Add Item TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { onAction(SearchQueryChanged(it)) },
            label = { Text("Add item") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onAction(KeyboardDonePressed) }
            ),
            leadingIcon = { // Optional: if you want a search icon
                Icon(Icons.Filled.Search, contentDescription = "Search or Add Item")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(itemsState.items, key = { it.itemName }) { item ->
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
private fun ShoppingListScreenLightPreview() {
    SAMTheme {
        ShoppingList(
            itemsState = ShoppingListItemsState(
                listOf(
                    "apple", "Banana", "Milk", "Eggs", "Cheese", "Chicken", "Beef",
                    "Pork", "Salmon", "Tuna", "Pasta", "Rice", "Bread", "Cereal",
                    "Coffee", "Tea", "Juice", "Soda", "Water"
                ).map { SingleItem(it) }
            )
        )
    }
}