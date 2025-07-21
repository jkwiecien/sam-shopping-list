package pl.techbrewery.sam.features.navigation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.techbrewery.sam.features.shoppinglist.ShoppingListScreen
import pl.techbrewery.sam.features.shoppinglist.ShoppingListViewModel
import pl.techbrewery.sam.features.stores.StorePressed
import pl.techbrewery.sam.features.stores.StoresScreen
import pl.techbrewery.sam.features.stores.StoresViewModel
import pl.techbrewery.sam.features.stores.editor.StoreEditorScreen
import pl.techbrewery.sam.features.stores.editor.StoreEditorViewModel
import pl.techbrewery.sam.kmp.repository.LocalizedResources
import pl.techbrewery.sam.kmp.routes.ScreenRoute
import pl.techbrewery.sam.shared.HeterogeneousIcon
import pl.techbrewery.sam.ui.shared.AppBar
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
                    navController.createGraph(startDestination = Screen.ShoppingList.route) {
                        composable(route = Screen.ShoppingList.route) {
                            ShoppingListScreen(shoppingListViewModel)
                        }
                        composable(route = ScreenRoute.Stores) {
                            StoresScreen(
                                storesViewModel,
                                onStorePressed = { store ->
                                    storeEditorViewModel.setStoreId(store.storeId)
                                    navController.navigate(ScreenRoute.StoreEditor)
                                },
                                onCreateStorePressed = {
                                    navController.navigate(ScreenRoute.StoreEditor)
                                }
                            )
                        }
                        composable(route = Screen.StoreEditor.route) {
                            StoreEditorScreen(storeEditorViewModel)
                        }
                    }
                }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val screenTitle = locRes.getScreenTitle(currentRoute)

                Scaffold(
                    topBar = {
                        AppBar(title = screenTitle)
                    },
                    bottomBar = {
                        BottomNavigationBar(navController, Modifier.navigationBarsPadding())
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
            is StorePressed -> navController.navigate(route = Screen.StoreEditor.route)
            // Handle other actions if needed
        }
    }

    private fun showScreen(screen: Screen) {

    }
}


@SuppressLint("RestrictedApi")
@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        navigationTopLevelRoutes.forEach { topLevelRoute ->
            NavigationBarItem(
                icon = {
                    HeterogeneousIcon(
                        topLevelRoute.icon,
                        contentDescription = topLevelRoute.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(topLevelRoute.title) },
                selected = currentDestination?.hierarchy?.any {
                    it.hasRoute(
                        topLevelRoute.route,
                        arguments = null
                    )
                } == true,
                onClick = {
                    navController.navigate(topLevelRoute.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}



