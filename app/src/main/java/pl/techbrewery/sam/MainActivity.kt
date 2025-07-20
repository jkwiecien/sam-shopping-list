package pl.techbrewery.sam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.techbrewery.sam.features.shoppinglist.ShoppingListViewModel
import pl.techbrewery.sam.features.shoppinglist.ShoppingListScreen
import pl.techbrewery.sam.ui.theme.SAMTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val shoppingListViewModel by viewModel<ShoppingListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAMTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Shopping List") },
                            actions = {
                                IconButton(onClick = { /* Handle add item to top bar action */ }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add Item")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        ShoppingBottomNavigation() // Assuming you have this composable
                    }
                ) { paddingValues ->
                    ShoppingListScreen(
                        viewModel = shoppingListViewModel,
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }
}




// Dummy Bottom Navigation - Replace with your actual implementation
@Composable
fun ShoppingBottomNavigation() {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List") },
            label = { Text("List") },
            selected = true, // Set based on current screen
            onClick = { /* Handle List navigation */ }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(R.drawable.ic_recipe_book_24dp),
                    contentDescription = "Recipes"
                )
            }, // Example icon
            label = { Text("Recipes") },
            selected = false,
            onClick = { /* Handle Recipes navigation */ }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(R.drawable.ic_store_24dp),
                    contentDescription = "Shops"
                )
            }, // Example icon
            label = { Text("Shops") },
            selected = false,
            onClick = { /* Handle Shops navigation */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = { /* Handle Settings navigation */ }
        )
    }
}



