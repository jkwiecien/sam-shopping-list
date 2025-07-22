package pl.techbrewery.sam.features.stores.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.kmp.database.entity.StoreDepartment
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.action_save
import pl.techbrewery.sam.ui.shared.ItemDragHandle
import pl.techbrewery.sam.ui.shared.PrimaryFilledButton
import pl.techbrewery.sam.ui.shared.stringResourceCompat
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun StoreEditorScreen(
    viewModel: StoreEditorViewModel,
) {
    StoreEditorScreenContent(
        storeName = viewModel.storeName
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreEditorScreenContent(
    storeName: String,
    modifier: Modifier = Modifier,
    departments: ImmutableList<StoreDepartment> = emptyList<StoreDepartment>().toImmutableList(),
    onAction: (Any) -> Unit = {}
) {
    // val uiState by viewModel.uiState.collectAsState() // Example of collecting state from ViewModel
    var layoutName by remember { mutableStateOf("") }
    // Replace with actual categories from your ViewModel or data source
    val categories = remember {
        listOf(
            "Fruits", "Vegetables", "Dairy", "Meat", "Bakery",
            "Frozen Foods", "Snacks", "Beverages", "Household"
        )
    }

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = layoutName,
            onValueChange = { onAction(StoreNameChanged(it)) },
            label = {
                Text(
                    text = storeName
                )
            }, // Create this string resource
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Customize the order of categories to match your shopping path in the store. Drag and drop categories to reflect your route.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier.weight(1f), // Allow the list to take available space
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryItem(categoryName = category)
            }
        }
        PrimaryFilledButton(
            modifier = Modifier.fillMaxWidth(),
            title = stringResourceCompat(Res.string.action_save, "Save"),
            onPressed = { onAction(SaveStorePressed) }
        )
    }
}

@Composable
fun CategoryItem(categoryName: String) {
    Row(
        modifier = Modifier
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
            storeName = "Lidl"
        )
    }
}