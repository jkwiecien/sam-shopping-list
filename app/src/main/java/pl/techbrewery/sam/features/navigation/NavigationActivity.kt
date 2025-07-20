package pl.techbrewery.sam.features.navigation

import android.R.attr.text
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.techbrewery.sam.R
import pl.techbrewery.sam.features.shoppinglist.ShoppingListScreen
import pl.techbrewery.sam.features.shoppinglist.ShoppingListViewModel
import pl.techbrewery.sam.features.stores.StoresViewModel
import pl.techbrewery.sam.ui.theme.SAMTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val navigationViewModel by viewModel<NavigationViewModel>()
    private val shoppingListViewModel by viewModel<ShoppingListViewModel>()
    private val storesViewModel by viewModel<StoresViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val selectedTab by navigationViewModel.selectedTabFlow.collectAsStateWithLifecycle()
            SAMTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = selectedTab.screenTitle) },
                            actions = {
                                IconButton(onClick = { /* Handle add item to top bar action */ }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add Item")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        ShoppingBottomNavigation(
                            selectedTab = selectedTab,
                            onAction = { navigationViewModel.onAction(it) }
                        ) // Assuming you have this composable
                    }
                ) { paddingValues ->
                    when (selectedTab) {
                        NavigationTab.SHOPPING_LIST -> ShoppingListScreen(
                            viewModel = shoppingListViewModel,
                            paddingValues = paddingValues
                        )

                        NavigationTab.RECIPES -> {}

                        NavigationTab.STORES -> {}
                        NavigationTab.SETTINGS -> {}
                    }
                }
            }
        }
    }

    private fun runActionsObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                storesViewModel.actionsFlow.collect { action ->
//                    when (action) {
//                        is CreateStoreBottomSheetState ->
//                    }
//                }
            }
        }
    }
}


// Dummy Bottom Navigation - Replace with your actual implementation
@Composable
fun ShoppingBottomNavigation(
    selectedTab: NavigationTab,
    onAction: (Any) -> Unit = {}
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List") },
            label = { Text(text = NavigationTab.SHOPPING_LIST.tabTitle) },
            selected = selectedTab.index() == 0,
            onClick = { onAction(NavigationTabPressed(NavigationTab.SHOPPING_LIST)) }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(R.drawable.ic_recipe_book_24dp),
                    contentDescription = "Recipes"
                )
            }, // Example icon
            label = { Text(text = NavigationTab.RECIPES.tabTitle) },
            selected = selectedTab.index() == 1,
            onClick = { onAction(NavigationTabPressed(NavigationTab.RECIPES)) }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(R.drawable.ic_store_24dp),
                    contentDescription = "Shops"
                )
            }, // Example icon
            label = { Text(text = NavigationTab.STORES.tabTitle) },
            selected = selectedTab.index() == 2,
            onClick = { onAction(NavigationTabPressed(NavigationTab.STORES)) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text(text = NavigationTab.SETTINGS.tabTitle) },
            selected = selectedTab.index() == 3,
            onClick = { onAction(NavigationTabPressed(NavigationTab.SETTINGS)) }
        )
    }
}



