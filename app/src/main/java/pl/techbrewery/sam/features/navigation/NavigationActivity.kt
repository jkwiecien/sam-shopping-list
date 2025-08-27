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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import io.github.aakira.napier.Napier
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.techbrewery.sam.features.auth.AuthModalContent
import pl.techbrewery.sam.features.auth.ToggleAuthModal
import pl.techbrewery.sam.features.navigation.ui.AppBarForRoute
import pl.techbrewery.sam.features.navigation.ui.BottomNavigationBar
import pl.techbrewery.sam.features.recipes.CreateRecipePressed
import pl.techbrewery.sam.features.recipes.RecipePressed
import pl.techbrewery.sam.features.recipes.RecipesScreen
import pl.techbrewery.sam.features.recipes.RecipesViewModel
import pl.techbrewery.sam.features.recipes.editor.RecipeEditorScreen
import pl.techbrewery.sam.features.recipes.editor.RecipeEditorViewModel
import pl.techbrewery.sam.features.recipes.editor.RecipeSaved
import pl.techbrewery.sam.features.shoppinglist.ShoppingListScreen
import pl.techbrewery.sam.features.shoppinglist.ShoppingListViewModel
import pl.techbrewery.sam.features.stores.CreateStorePressed
import pl.techbrewery.sam.features.stores.StorePressed
import pl.techbrewery.sam.features.stores.StoresScreen
import pl.techbrewery.sam.features.stores.StoresViewModel
import pl.techbrewery.sam.features.stores.editor.StoreEditorScreen
import pl.techbrewery.sam.features.stores.editor.StoreEditorViewModel
import pl.techbrewery.sam.features.stores.editor.StoreUpdated
import pl.techbrewery.sam.kmp.routes.ScreenRoute
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.button_title_new_recipe
import pl.techbrewery.sam.resources.button_title_new_store
import pl.techbrewery.sam.shared.HeterogeneousVectorIcon
import pl.techbrewery.sam.ui.shared.PrimaryFilledButton
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.stringResourceCompat
import pl.techbrewery.sam.ui.theme.SAMTheme

@OptIn(ExperimentalMaterial3Api::class)
class NavigationActivity : ComponentActivity() {

    private val navigationViewModel by viewModel<NavigationViewModel>()
    private val shoppingListViewModel by viewModel<ShoppingListViewModel>()
    private val storesViewModel by viewModel<StoresViewModel>()
    private val storeEditorViewModel by viewModel<StoreEditorViewModel>()
    private val recipesViewModel by viewModel<RecipesViewModel>()
    private val recipeEditorViewModel by viewModel<RecipeEditorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAMTheme {
                var hideAppBar by remember { mutableStateOf(false) }
                fun resetAppBarVisibility() {
                    hideAppBar = false
                }

                val sheetState = rememberModalBottomSheetState()
                val showAuthModal = navigationViewModel.showAuthModal

                if (showAuthModal) {
                    ModalBottomSheet(
                        onDismissRequest = { navigationViewModel.onAction(ToggleAuthModal(false)) },
                        sheetState = sheetState
                    ) {
                        AuthModalContent(
                            onAction = { navigationViewModel.onAction(it) }
                        )
                    }
                }

                val navController = rememberNavController()
                val navGraph = remember(navController) {
                    navController.createGraph(startDestination = ScreenRoute.ShoppingList) {
                        composable(route = ScreenRoute.ShoppingList) {
                            ShoppingListScreen(
                                viewModel = shoppingListViewModel,
                                modifier = Modifier.padding(vertical = Spacing.Small),
                                onStoreDropdownVisibilityChanged = { visible ->
                                    tempLog("onStoreDropdownVisibilityChanged: $visible")
//                                    hideAppBar = !visible //fixme this hides app bar when there is just one store
                                },
                                onExternalAction = { action ->
                                    navigationViewModel.onAction(action)
                                })
                        }
                        composable(route = ScreenRoute.Stores) {
                            resetAppBarVisibility()
                            StoresScreen(
                                storesViewModel,
                                onExternalAction = { action ->
                                    when (action) {
                                        is StorePressed -> {
                                            storeEditorViewModel.setStoreId(action.store.storeId)
                                            navController.navigate(ScreenRoute.StoreEditor)
                                        }

                                        is CreateStorePressed -> {
                                            storeEditorViewModel.clearState()
                                            navController.navigate(ScreenRoute.StoreEditor)
                                        }
                                    }
                                }
                            )
                        }
                        composable(route = ScreenRoute.StoreEditor) {
                            resetAppBarVisibility()
                            StoreEditorScreen(
                                storeEditorViewModel,
                                onExternalAction = { action ->
                                    when (action) {
                                        is StoreUpdated -> navController.navigate(ScreenRoute.Stores)
                                    }
                                }
                            )
                        }
                        composable(route = ScreenRoute.Recipes) {
                            resetAppBarVisibility()
                            RecipesScreen(
                                recipesViewModel,
                                onExternalAction = { action ->
                                    when (action) {
                                        is CreateRecipePressed -> {
                                            recipeEditorViewModel.clearState()
                                            navController.navigate(ScreenRoute.RecipeEditor)
                                        }

                                        is RecipePressed -> {
                                            recipeEditorViewModel.setRecipe(action.recipe)
                                            navController.navigate(ScreenRoute.RecipeEditor)
                                        }
                                    }
                                }
                            )
                        }
                        composable(route = ScreenRoute.RecipeEditor) {
                            resetAppBarVisibility()
                            RecipeEditorScreen(
                                recipeEditorViewModel,
                                onExternalAction = { action ->
                                    when (action) {
                                        is RecipeSaved -> navController.navigate(ScreenRoute.Recipes)
                                    }
                                }
                            )
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
                                onNavigateUp = { navController.navigateUp() },
                                onAction = when (currentRoute) {
                                    ScreenRoute.ShoppingList -> { action ->
                                        shoppingListViewModel.onAction(action)
                                    }

                                    else -> { action ->
                                        Napier.w { "Unresolved action in AppBar: $action" }
                                    }
                                }
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
                                        ScreenRoute.Stores -> {
                                            storeEditorViewModel.clearState()
                                            navController.navigate(route = ScreenRoute.StoreEditor)
                                        }

                                        ScreenRoute.Recipes -> {
                                            recipeEditorViewModel.clearState()
                                            navController.navigate(route = ScreenRoute.RecipeEditor)
                                        }
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
}

@Composable
private fun FloatingActionButtonForRoute(
    route: String,
    onFloatingActionButtonPressed: (String) -> Unit
) {
    when (route) {
        ScreenRoute.Stores -> Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryFilledButton(
                title = stringResourceCompat(Res.string.button_title_new_store, "New store"),
                onPressed = { onFloatingActionButtonPressed(route) },
                leadingIcon = HeterogeneousVectorIcon.VectorIcon(Icons.Filled.Add)
            )
        }

        ScreenRoute.Recipes -> Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryFilledButton(
                title = stringResourceCompat(Res.string.button_title_new_recipe, "New recipe"),
                onPressed = { onFloatingActionButtonPressed(route) },
                leadingIcon = HeterogeneousVectorIcon.VectorIcon(Icons.Filled.Add)
            )
        }
    }
}







