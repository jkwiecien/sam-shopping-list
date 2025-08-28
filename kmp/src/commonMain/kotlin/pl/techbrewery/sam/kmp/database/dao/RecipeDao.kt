package pl.techbrewery.sam.kmp.database.dao

import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pl.techbrewery.sam.kmp.database.entity.RecipeItem
import pl.techbrewery.sam.kmp.database.bundles.RecipeWithItems

@Dao
interface RecipeDao {

    // --- Insert Operations ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleItem(singleItem: SingleItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeIngredient(ingredient: RecipeItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeIngredients(ingredients: List<RecipeItem>)

    // --- Query Operations (Flows) ---

    @Transaction
    @Query("SELECT * FROM recipes")
    fun getRecipesWithItems(): Flow<List<RecipeWithItems>>

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<Recipe>

    @Transaction
    @Query("SELECT * FROM recipes WHERE name LIKE :query || '%'")
    fun getRecipesWithItemsFlow(query: String): Flow<List<RecipeWithItems>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE name LIKE :recipeName ")
    fun getRecipeWithItemsByName(recipeName: String): RecipeWithItems?

    @Query("SELECT * FROM recipes WHERE recipe_id = :recipeId") // Changed bundle_id to recipe_id
    suspend fun getRecipe(recipeId: Long): Recipe?

    @Query("SELECT * FROM recipes WHERE name = :recipName")
    suspend fun getRecipeByName(recipName: String): Recipe?

    @Transaction
    @Query("SELECT * FROM recipes WHERE recipe_id = :recipeId") // Changed bundle_id to recipe_id, param bundleId to recipeId
    fun getRecipeWithSingleItemsFlow(recipeId: Long): Flow<RecipeWithItems?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE recipe_id = :recipeId") // Changed bundle_id to recipe_id, param bundleId to recipeId
    suspend fun getRecipeWithSingleItems(recipeId: Long): RecipeWithItems?

    @Query(
        """
        SELECT si.* FROM single_items si
        INNER JOIN recipe_item_join bicr ON si.item_name = bicr.item_name_join
        WHERE bicr.recipe_id_join = :recipeId
    """ // Changed bicr.bundle_id_join to bicr.recipe_id_join, param bundleId to recipeId
    )
    fun getSingleItemsForRecipe(recipeId: Long): Flow<List<SingleItem>>

    @Query("SELECT * FROM recipe_item_join WHERE recipe_id_join = :recipeId") // Changed bundle_id_join to recipe_id_join
    suspend fun getIngredients(recipeId: Long): List<RecipeItem>

    // --- Delete Operations ---
    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipeIngredients(ingredients: List<RecipeItem>)
}
