package pl.techbrewery.sam.kmp.repository

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.kmp.cloud.CloudRepository
import pl.techbrewery.sam.kmp.database.KmpDatabase
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.RecipeItem
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.database.bundles.RecipeWithItems
import pl.techbrewery.sam.kmp.utils.getCurrentTime
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_recipe_already_exists
import java.sql.SQLIntegrityConstraintViolationException

class RecipeRepository(
    private val db: KmpDatabase,
    private val cloud: CloudRepository
) {
    private val recipeDao = db.recipeDao()

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

    suspend fun insertRecipe(recipeName: String, items: List<SingleItem>) = coroutineScope {
        if (recipeDao.getRecipeByName(recipeName) != null) {
            throw SQLIntegrityConstraintViolationException(getString(Res.string.error_recipe_already_exists))
        }
        val recipe = Recipe(
            name = recipeName
        )
        val createdRecipeId = recipeDao.insertRecipe(recipe)
        val recipeToSave = recipe.copy(recipeId = createdRecipeId)
        cloud.cloudUpdater?.let { launch { it.saveRecipe(recipeToSave) } }
        items.forEach { item ->
            val join = RecipeItem(
                recipeId = createdRecipeId,
                itemName = item.itemName
            )
            recipeDao.insertRecipeIngredient(join)
            recipeToSave.cloudId?.let { recipeCloudId ->
                cloud.cloudUpdater?.let { launch { it.saveRecipeItem(join, recipeCloudId) } }
            }
        }
    }

    suspend fun updateRecipe(recipe: Recipe, recipeName: String, items: List<SingleItem>) = coroutineScope {
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
                    RecipeItem(
                        recipeId = entityRecipeId,
                        itemName = item.itemName
                    )
                }

            recipeDao.deleteRecipeIngredients(ingredientsToRemove)
            ingredientsToRemove.forEach { item ->
                cloud.cloudUpdater?.let { launch { it.deleteRecipeItem(item) } }
            }
            recipeDao.insertRecipeIngredients(ingredientsToAdd)
            recipe.cloudId?.let { recipeCloudId ->
                ingredientsToAdd.forEach { item ->
                    cloud.cloudUpdater?.let { launch { it.saveRecipeItem(item, recipeCloudId) } }
                }
            }
            val updatedRecipe = recipeWithItems.recipe.copy(
                name = recipeName,
                updatedAt = getCurrentTime()
            )
            recipeDao.updateRecipe(updatedRecipe)
            cloud.cloudUpdater?.let { launch { it.saveRecipe(updatedRecipe) } }
        }
    }

    suspend fun deleteRecipeWithIngredients(recipeWithItems: RecipeWithItems) = coroutineScope {
        val ingredients = recipeWithItems.items.map { RecipeItem(recipeId = recipeWithItems.recipe.recipeId, itemName = it.itemName) }
        recipeDao.deleteRecipeIngredients(ingredients)
        recipeDao.deleteRecipe(recipeWithItems.recipe)
        cloud.cloudUpdater?.let { launch { it.deleteRecipe(recipeWithItems.recipe) } }
    }
}
