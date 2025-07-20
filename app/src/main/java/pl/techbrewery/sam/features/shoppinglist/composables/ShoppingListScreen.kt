package pl.techbrewery.sam.features.shoppinglist.composables

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
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.features.shoppinglist.ShoppingListScreenState
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.ui.theme.SAMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    state: ShoppingListScreenState = ShoppingListScreenState(),
    onCheckboxChecked: (itemName: String) -> Unit = {},
    paddingValues: PaddingValues = PaddingValues()
) {
    var searchText by remember { mutableStateOf("") }
    // Replace this with your actual shopping list data and logic

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        // Search/Add Item TextField
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Add item") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            leadingIcon = { // Optional: if you want a search icon
                Icon(Icons.Filled.Search, contentDescription = "Search or Add Item")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(state.items) { item ->
                ShoppingListItem(
                    itemName = item.itemName,
                    onCheckboxChecked =onCheckboxChecked
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ShoppingListItem(
    itemName: String,
    onCheckboxChecked: (itemName: String) -> Unit,
) {
    var checked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // Increased padding for better touch target
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start // Align items to the start
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked ->
                if (checked) onCheckboxChecked(itemName)
            }
        )
        Text(
            text = itemName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ShoppingListScreenLightPreview() {
    SAMTheme  {
        ShoppingListScreen(
            state = ShoppingListScreenState(
                listOf(
                    "Apple", "Banana", "Milk", "Eggs", "Cheese", "Chicken", "Beef",
                    "Pork", "Salmon", "Tuna", "Pasta", "Rice", "Bread", "Cereal",
                    "Coffee", "Tea", "Juice", "Soda", "Water"
                ).map { SingleItem(it) }
            )
        )
    }
}