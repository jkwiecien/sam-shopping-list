package pl.techbrewery.sam.features.recipes.editor

import pl.techbrewery.sam.kmp.database.entity.SingleItem

internal class RecipeItemDismissed(val item: SingleItem)
internal class RecipeNameChanged(val name: String)
internal object SaveRecipePressed
internal object IngredientNameFieldKeyboardDonePressed
object RecipeSaved