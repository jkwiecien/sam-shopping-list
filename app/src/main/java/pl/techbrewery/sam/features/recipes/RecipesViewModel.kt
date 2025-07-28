package pl.techbrewery.sam.features.recipes

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.techbrewery.sam.kmp.database.pojo.ItemBundleWithItems
import pl.techbrewery.sam.kmp.repository.RecipeRepository
import pl.techbrewery.sam.shared.BaseViewModel

class RecipesViewModel(
    private val repository: RecipeRepository
): BaseViewModel() {

    val recipesFlow = repository.getRecipesWithItemsFlow()
        .map { it.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<ItemBundleWithItems>().toImmutableList()
        )

    override fun onAction(action: Any) {
        when (action) {
            is RecipeDismissed -> deleteRecipe(action.recipe)
        }
    }

    fun deleteRecipe(recipe: ItemBundleWithItems) {
        viewModelScope.launch {
            repository.deleteRecipeWithIngredients(recipe)
        }
    }
    
}