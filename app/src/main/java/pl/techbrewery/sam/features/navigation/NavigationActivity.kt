package pl.techbrewery.sam.features.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.techbrewery.sam.features.navigation.ui.BottomNavigationBar
import pl.techbrewery.sam.features.shoppinglist.ShoppingListScreen
import pl.techbrewery.sam.features.shoppinglist.ShoppingListViewModel
import pl.techbrewery.sam.features.stores.CreateStorePressed
import pl.techbrewery.sam.features.stores.StorePressed
import pl.techbrewery.sam.features.stores.StoresScreen
import pl.techbrewery.sam.features.stores.StoresViewModel
import pl.techbrewery.sam.features.stores.editor.StoreEditorScreen
import pl.techbrewery.sam.features.stores.editor.StoreEditorViewModel
import pl.techbrewery.sam.kmp.repository.LocalizedResources
import pl.techbrewery.sam.kmp.routes.ScreenRoute
import pl.techbrewery.sam.shared.FloatingActionButtonPressed
import pl.techbrewery.sam.shared.HeterogeneousVectorIcon
import pl.techbrewery.sam.ui.shared.AppBar
import pl.techbrewery.sam.ui.shared.PrimaryFilledButton
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.theme.SAMTheme

@OptIn(ExperimentalMaterial3Api::class)
class NavigationActivity : ComponentActivity() {
    private val locRes by inject<LocalizedResources>()

    private val navigationViewModel by viewModel<NavigationViewModel>()
    private val shoppingListViewModel by viewModel<ShoppingListViewModel>()
    private val storesViewModel by viewModel<StoresViewModel>()
    private val storeEditorViewModel by viewModel<StoreEditorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAMTheme {
                val navController = rememberNavController()
                val navGraph = remember(navController) {
                    navController.createGraph(startDestination = ScreenRoute.ShoppingList) {
                        composable(route = ScreenRoute.ShoppingList) {
                            ShoppingListScreen(shoppingListViewModel)
                        }
                        composable(route = ScreenRoute.Stores) {
                            StoresScreen(
                                storesViewModel,
                                onNavigationAction = { action ->
                                    when (action) {
                                        is StorePressed -> {
                                            storeEditorViewModel.setStoreId(action.store.storeId)
                                            onNavigationAction(action, navController)
                                        }

                                        is CreateStorePressed -> navController.navigate(ScreenRoute.StoreEditor)
                                    }
                                }
                            )
                        }
                        composable(route = ScreenRoute.StoreEditor) {
                            StoreEditorScreen(storeEditorViewModel)
                        }
                    }
                }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val screenTitle = locRes.getScreenTitle(currentRoute)

                Scaffold(
                    topBar = {
                        AppBar(
                            title = screenTitle,
                            canNavigateBack = navController.previousBackStackEntry != null,
                            navigateUp = { navController.navigateUp() }
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(navController, Modifier.navigationBarsPadding())
                    },
                    floatingActionButton = {
                        currentRoute?.let {
                            FloatingActionButtonForRoute(
                                route = currentRoute,
                                onFloatingActionButtonPressed = { route ->
                                    when (route) {
                                         ScreenRoute.Stores -> navController.navigate(route = ScreenRoute.StoreEditor)
                                    }
                                }
                            )
                        }

                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        graph = navGraph,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun onNavigationAction(action: Any, navController: NavController) {
        when (action) {
            is StorePressed -> navController.navigate(route = ScreenRoute.StoreEditor)
            // Handle other actions if needed
        }
    }
}

@Composable
private fun FloatingActionButtonForRoute(
    route: String,
    onFloatingActionButtonPressed: (String) -> Unit
) {
    if (route == ScreenRoute.Stores) {
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryFilledButton(
                title = "New Shop",
                onPressed = { onFloatingActionButtonPressed(route) },
                leadingIcon = HeterogeneousVectorIcon.VectorIcon(Icons.Filled.Add)
            )
        }
    }
}





