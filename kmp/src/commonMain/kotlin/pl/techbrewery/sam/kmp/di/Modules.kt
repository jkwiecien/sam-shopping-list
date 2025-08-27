package pl.techbrewery.sam.kmp.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.getDatabaseBuilder
import pl.techbrewery.sam.kmp.database.getRoomDatabase
import pl.techbrewery.sam.kmp.repository.RecipeRepository
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository
import pl.techbrewery.sam.kmp.repository.StoreRepository

val kmpModules: List<Module>
    get() = listOf(
        databaseModule,
        repositoryModule
    )

private val databaseModule = module {
    single<KmpDatabase> {
        val builder = getDatabaseBuilder()
        getRoomDatabase(builder)
    }

    single { get<KmpDatabase>().singleItemDao() }
    single { get<KmpDatabase>().recipeDao() }
    single { get<KmpDatabase>().storeDao() }
}

private val repositoryModule = module {
    single { ShoppingListRepository(get<KmpDatabase>()) }
    single { StoreRepository(get<KmpDatabase>()) }
    single { RecipeRepository(get<KmpDatabase>()) }
}