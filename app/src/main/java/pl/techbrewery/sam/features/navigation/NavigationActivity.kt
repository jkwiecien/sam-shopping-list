package pl.techbrewery.sam.features.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import org.jetbrains.compose.resources.stringResource
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
import pl.techbrewery.sam.kmp.routes.ScreenRoute
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.screen_title_recipes
import pl.techbrewery.sam.resources.screen_title_settings
import pl.techbrewery.sam.resources.screen_title_shopping_list
import pl.techbrewery.sam.resources.screen_title_store_editor
import pl.techbrewery.sam.resources.screen_title_stores
import pl.techbrewery.sam.shared.HeterogeneousVectorIcon
import pl.techbrewery.sam.ui.shared.AppBar
import pl.techbrewery.sam.ui.shared.AppBarHeight
import pl.techbrewery.sam.ui.shared.PrimaryFilledButton
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.statusBarAsTopPadding
import pl.techbrewery.sam.ui.theme.SAMTheme

@OptIn(ExperimentalMaterial3Api::class)
class NavigationActivity : ComponentActivity() {

    private val navigationViewModel by viewModel<NavigationViewModel>()
    private val shoppingListViewModel by viewModel<ShoppingListViewModel>()
    private val storesViewModel by viewModel<StoresViewModel>()
    private val storeEditorViewModel by viewModel<StoreEditorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAMTheme {
                var hideAppBar by remember { mutableStateOf(false) }
                fun resetAppBarVisibility() {
                    hideAppBar = false
                }

                val navController = rememberNavController()
                val navGraph = remember(navController) {
                    navController.createGraph(startDestination = ScreenRoute.ShoppingList) {
                        composable(route = ScreenRoute.ShoppingList) {
                            ShoppingListScreen(
                                viewModel = shoppingListViewModel,
                                modifier = Modifier.padding(vertical = Spacing.Small),
                                onListScrollChanged = { scrolled ->
                                    tempLog("onListScrollChanged: $scrolled")
                                    hideAppBar = scrolled
                                })
                        }
                        composable(route = ScreenRoute.Stores) {
                            resetAppBarVisibility()
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
                            resetAppBarVisibility()
                            StoreEditorScreen(storeEditorViewModel)
                        }
                    }
                }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route


                Scaffold(
                    topBar = {
                        currentRoute?.let {
                            AppBarForRoute(
                                route = it,
                                hideBar = hideAppBar,
                                canNavigateBack = navController.previousBackStackEntry != null,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
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
                title = "New store",
                onPressed = { onFloatingActionButtonPressed(route) },
                leadingIcon = HeterogeneousVectorIcon.VectorIcon(Icons.Filled.Add)
            )
        }
    }
}

@Composable
private fun AppBarForRoute(
    route: String,
    canNavigateBack: Boolean,
    hideBar: Boolean = false,
    onNavigateUp: () -> Unit = {}
) {
    val screenTitle = when (route) {
        ScreenRoute.ShoppingList -> stringResource(Res.string.screen_title_shopping_list)
        ScreenRoute.Recipes -> stringResource(Res.string.screen_title_recipes)
        ScreenRoute.Stores -> stringResource(Res.string.screen_title_stores)
        ScreenRoute.Settings -> stringResource(Res.string.screen_title_settings)
        ScreenRoute.StoreEditor -> stringResource(Res.string.screen_title_store_editor)
        else -> "" // Or handle unknown routes appropriately
    }
    val height: Dp = if (hideBar) 0.dp else AppBarHeight
    val modifier = Modifier
        .animateContentSize(animationSpec = tween(durationMillis = 300))
        .fillMaxWidth().let {
            if (height > 0.dp) {
                it.height(height + statusBarAsTopPadding())
            } else {
                it
                    .statusBarsPadding()
                    .height(height)
            }
        }
    AppBar(
        title = screenTitle,
        canNavigateBack = canNavigateBack,
        modifier = modifier,
        navigateUp = onNavigateUp
    )
}







