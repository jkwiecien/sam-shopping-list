package pl.techbrewery.sam.kmp.repository

import androidx.sqlite.throwSQLiteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.ItemBundle
import pl.techbrewery.sam.kmp.database.entity.ItemBundleJoin
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.pojo.ItemBundleWithItems
import pl.techbrewery.sam.kmp.utils.getCurrentTime
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_recipe_already_exists
import java.sql.SQLIntegrityConstraintViolationException

class RecipeRepository(
    private val dp: KmpDatabase
) {
    private val recipeDao = dp.itemBundleDao()

    fun getRecipesWithItemsFlow(): Flow<List<ItemBundleWithItems>> =
        recipeDao.getItemBundlesWithSingleItems()

    fun getRecipesFlow(): Flow<List<ItemBundle>> = recipeDao
        .getItemBundlesWithSingleItems()
        .map { bundlesWithItems ->
            bundlesWithItems.map { it.bundle }
        }

    suspend fun getRecipeWithItems(recipeId: Long): ItemBundleWithItems? {
        return recipeDao.getItemBundleWithSingleItems(recipeId)
    }

    suspend fun getRecipe(recipeName: String): ItemBundle? {
        return recipeDao.getRecipeByName(recipeName)
    }

    suspend fun insertRecipe(recipeName: String, items: List<SingleItem>) {
        if (recipeDao.getRecipeByName(recipeName) != null) {
            throw SQLIntegrityConstraintViolationException(getString(Res.string.error_recipe_already_exists))
        }
        val recipe = ItemBundle(
            name = recipeName
        )
        val createdRecipeId = recipeDao.insertItemBundle(recipe)
        items.forEach { item ->
            val join = ItemBundleJoin(
                bundleId = createdRecipeId,
                itemName = item.itemName
            )
            recipeDao.insertRecipeIngredient(join)
        }
    }

    suspend fun updateRecipe(recipe: ItemBundle, recipeName: String, items: List<SingleItem>) {
        val recipeId = recipe.bundleId
        recipeDao.getItemBundleWithSingleItems(recipeId)?.let { recipeWithItems ->
            val currentIngredients = recipeDao.getIngredients(recipeId)
            val ingredientsToRemove =
                currentIngredients
                    .filter { ingredient ->
                        ingredient.itemName !in items
                            .map { item -> item.itemName }
                    }
            val ingredientsToAdd = items
                .filter { item ->
                    item.itemName !in currentIngredients
                        .map { ingredient -> ingredient.itemName }
                }.map { item ->
                    ItemBundleJoin(
                        bundleId = recipeId,
                        itemName = item.itemName
                    )
                }

            recipeDao.deleteRecipeIngredients(ingredientsToRemove)
            recipeDao.insertRecipeIngredients(ingredientsToAdd)
            val updatedRecipe = recipeWithItems.bundle.copy(
                name = recipeName,
                updatedAt = getCurrentTime()
            )
            recipeDao.updateRecipe(updatedRecipe)
        }
    }

    suspend fun deleteRecipeWithIngredients(recipe: ItemBundleWithItems) {
        val ingredients = recipe.items.map { ItemBundleJoin(recipe.bundle.bundleId,it.itemName) }
        recipeDao.deleteRecipeIngredients(ingredients)
        recipeDao.deleteRecipe(recipe.bundle)
    }
}