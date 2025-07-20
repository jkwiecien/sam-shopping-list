package pl.techbrewery.sam.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.techbrewery.sam.features.shoppinglist.ShoppingListViewModel
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
    viewModel { ShoppingListViewModel(get()) }
}