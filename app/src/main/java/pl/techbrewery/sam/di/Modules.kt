package pl.techbrewery.sam.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.techbrewery.sam.features.auth.AuthRepository
import pl.techbrewery.sam.features.auth.AuthViewModel
import pl.techbrewery.sam.features.navigation.NavigationViewModel
import pl.techbrewery.sam.features.recipes.RecipesViewModel
import pl.techbrewery.sam.features.recipes.editor.RecipeEditorViewModel
import pl.techbrewery.sam.features.shoppinglist.ShoppingListViewModel
import pl.techbrewery.sam.features.stores.StoresViewModel
import pl.techbrewery.sam.features.stores.editor.StoreEditorViewModel
import pl.techbrewery.sam.kmp.cloud.CloudRepository
import pl.techbrewery.sam.kmp.cloud.CloudSyncService
import pl.techbrewery.sam.kmp.repository.RecipeRepository
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository

val appModules: List<Module>
    get() = listOf(
        repositoryModule,
        viewModelModule
    )

private val repositoryModule = module {
    single { CloudSyncService(get(), get()) }
    single { CloudRepository(get()) }
    single { ShoppingListRepository(get(), get()) }
    single { AuthRepository(androidContext()) }
}

private val viewModelModule = module {
    viewModel { NavigationViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { ShoppingListViewModel(get(), get(), get(), get()) }
    viewModel { StoresViewModel(get()) }
    viewModel { StoreEditorViewModel(get()) }
    viewModel { RecipesViewModel(get()) }
    viewModel { RecipeEditorViewModel(get(), get()) }
}