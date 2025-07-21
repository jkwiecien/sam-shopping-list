package pl.techbrewery.sam.features.stores.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.features.stores.CategoryItem
import pl.techbrewery.sam.features.stores.StoresViewModel
import pl.techbrewery.sam.ui.shared.AppBar
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun StoreEditorScreen(
    viewModel: StoreEditorViewModel,
) {
    StoreEditorScreeContent(
        title = viewModel.screenTitle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreEditorScreeContent(
    title: String
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

    Scaffold(
        topBar = {
            AppBar(
                title = title
            )
        },
        bottomBar = {
            Button(
                onClick = { /* TODO: Implement save layout logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = layoutName,
                onValueChange = { layoutName = it },
                label = { Text("Store name") }, // Create this string resource
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StoresScreenPreview() {
    SAMTheme { // Wrap with your app's theme if you have one
        StoreEditorScreeContent(
            title = "Store layout"
        )
    }
}