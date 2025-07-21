package pl.techbrewery.sam.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.techbrewery.sam.features.navigation.NavigationViewModel
import pl.techbrewery.sam.features.shoppinglist.ShoppingListViewModel
import pl.techbrewery.sam.features.stores.StoresViewModel
import pl.techbrewery.sam.features.stores.editor.StoreEditorViewModel
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository

val appModules: List<Module>
    get() = listOf(
        repositoryModule,
        viewModelModule
    )

private val repositoryModule = module {
    single { ShoppingListRepository(get()) }
}

private val viewModelModule = module {
    viewModel { NavigationViewModel(get()) }
    viewModel { ShoppingListViewModel(get(), get()) }
    viewModel { StoresViewModel(get()) }
    viewModel { StoreEditorViewModel(get()) }
}