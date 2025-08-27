package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.RecipeJoin
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.bundles.RecipeWithItems
import pl.techbrewery.sam.kmp.utils.getCurrentTime
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_recipe_already_exists
import java.sql.SQLIntegrityConstraintViolationException

class RecipeRepository(
    private val dp: KmpDatabase
) {
    private val recipeDao = dp.recipeDao()

    fun getRecipesWithItemsFlow(): Flow<List<RecipeWithItems>> =
        recipeDao.getRecipesWithItems()

    fun getRecipesFlow(): Flow<List<Recipe>> = recipeDao
        .getRecipesWithItems()
        .map { bundlesWithItems ->
            bundlesWithItems.map { it.recipe }
        }

    suspend fun getRecipeWithItems(recipeId: Long): RecipeWithItems? {
        return recipeDao.getRecipeWithSingleItems(recipeId)
    }

    suspend fun getRecipe(recipeName: String): Recipe? {
        return recipeDao.getRecipeByName(recipeName)
    }

    suspend fun insertRecipe(recipeName: String, items: List<SingleItem>) {
        if (recipeDao.getRecipeByName(recipeName) != null) {
            throw SQLIntegrityConstraintViolationException(getString(Res.string.error_recipe_already_exists))
        }
        val recipe = Recipe(
            name = recipeName
        )
        val createdRecipeId = recipeDao.insertRecipe(recipe)
        items.forEach { item ->
            val join = RecipeJoin(
                recipeId = createdRecipeId,
                itemName = item.itemName
            )
            recipeDao.insertRecipeIngredient(join)
        }
    }

    suspend fun updateRecipe(recipe: Recipe, recipeName: String, items: List<SingleItem>) {
        val entityRecipeId = recipe.recipeId
        recipeDao.getRecipeWithSingleItems(entityRecipeId)?.let { recipeWithItems ->
            val currentIngredients = recipeDao.getIngredients(entityRecipeId)
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
                    RecipeJoin(
                        recipeId = entityRecipeId,
                        itemName = item.itemName
                    )
                }

            recipeDao.deleteRecipeIngredients(ingredientsToRemove)
            recipeDao.insertRecipeIngredients(ingredientsToAdd)
            val updatedRecipe = recipeWithItems.recipe.copy(
                name = recipeName,
                updatedAt = getCurrentTime()
            )
            recipeDao.updateRecipe(updatedRecipe)
        }
    }

    suspend fun deleteRecipeWithIngredients(recipeWithItems: RecipeWithItems) {
        val ingredients = recipeWithItems.items.map { RecipeJoin(recipeWithItems.recipe.recipeId,it.itemName) }
        recipeDao.deleteRecipeIngredients(ingredients)
        recipeDao.deleteRecipe(recipeWithItems.recipe)
    }
}
