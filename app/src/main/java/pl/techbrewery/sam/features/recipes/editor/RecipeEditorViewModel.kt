package pl.techbrewery.sam.features.recipes.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import pl.techbrewery.sam.extensions.capitalize
import pl.techbrewery.sam.features.shoppinglist.SuggestedItemSelected
import pl.techbrewery.sam.kmp.database.bundles.RecipeWithItems // Changed import
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.kmp.model.SuggestedItem
import pl.techbrewery.sam.kmp.repository.RecipeRepository
import pl.techbrewery.sam.kmp.repository.ShoppingListRepository
import pl.techbrewery.sam.kmp.utils.CRUD
import pl.techbrewery.sam.kmp.utils.IgnoredException
import pl.techbrewery.sam.kmp.utils.ItemAlreadyOnListException
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.error_item_already_in_recipe
import pl.techbrewery.sam.shared.BaseViewModel
import pl.techbrewery.sam.shared.OnItemTextFieldFocusChanged
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.ui.shared.DropdownItem
import java.sql.SQLIntegrityConstraintViolationException

class RecipeEditorViewModel(
    private val repository: RecipeRepository,
    private val shoppingList: ShoppingListRepository
) : BaseViewModel() {
    private val searchQueryMutableFlow: MutableStateFlow<String> = MutableStateFlow("")
    internal val searchQueryFlow: StateFlow<String> = searchQueryMutableFlow

    private var crud: CRUD = CRUD.INSERT

    var recipeNameTextFieldError: String? by mutableStateOf(null)
        private set
    var itemNameTextFieldError: String? by mutableStateOf(null)
        private set

    private var editedRecipe: RecipeWithItems? = null // Changed type

    private val itemTextFieldFocusedMutableFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val itemsMutableFlow: MutableStateFlow<List<SingleItem>> = MutableStateFlow(emptyList())
    internal val itemsFlow: StateFlow<ImmutableList<SingleItem>> =
        itemsMutableFlow
            .map { items ->
                items.toImmutableList()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<SingleItem>().toImmutableList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val suggestedItemsDropdownItems: StateFlow<ImmutableList<DropdownItem<SuggestedItem>>> =
        combine(searchQueryFlow, itemTextFieldFocusedMutableFlow) { query, focused ->
            query to focused
        }
            .flatMapLatest { dataPair ->
                val query = dataPair.first
                val focused = dataPair.second
                if (query.isNotEmpty() && focused) {
                    shoppingList.getSuggestedRecipeIngredients(query)
                } else {
                    flowOf(emptyList())
                }
            }
            .map { suggestedItems ->
                suggestedItems.map { item ->
                    DropdownItem(
                        item = item,
                        text = item.itemName
                    )
                }
            }
            .map { it.toImmutableList() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList<DropdownItem<SuggestedItem>>().toImmutableList()
            )

    private val recipeNameMutableFlow: MutableStateFlow<String> = MutableStateFlow("")
    val recipeNameFlow: StateFlow<String> = recipeNameMutableFlow
    val saveButtonEnabledFlow: StateFlow<Boolean> =
        combine(recipeNameFlow, itemsFlow) { recipeName, items ->
            recipeName.isNotBlank() && items.isNotEmpty()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    init {
        viewModelScope.launch {
            launch {
                searchQueryFlow.collect {
                    itemNameTextFieldError = null
                }
            }
            launch {
                recipeNameFlow.collect {
                    recipeNameTextFieldError = null
                }
            }
        }
    }

    fun clearState() {
        crud = CRUD.INSERT
        editedRecipe = null
        searchQueryMutableFlow.value = ""
        recipeNameMutableFlow.value = ""
        itemsMutableFlow.value = emptyList()
    }

    fun setRecipe(recipe: RecipeWithItems) { // Changed parameter type
        crud = CRUD.UPDATE
        editedRecipe = recipe
        recipeNameMutableFlow.value = recipe.recipe.name // Changed from recipe.bundle.name
        itemsMutableFlow.value = recipe.items
    }

    private fun onSearchQueryChanged(query: String) {
        searchQueryMutableFlow.value = query
    }

    override fun onAction(action: Any) {
        when (action) {
            is RecipeNameChanged -> recipeNameMutableFlow.value = action.name.capitalize()
            is SearchQueryChanged -> onSearchQueryChanged(action.query)
            is SuggestedItemSelected -> addItem(action.item)
            is RecipeItemDismissed -> removeItem(action.item)
            is SaveRecipePressed -> saveRecipe()
            is IngredientNameFieldKeyboardDonePressed -> addItem()
            is OnItemTextFieldFocusChanged -> itemTextFieldFocusedMutableFlow.value = action.focused
        }
    }

    private fun removeItem(item: SingleItem) {
        viewModelScope.launch(Dispatchers.Main) {
            itemsMutableFlow.value = itemsMutableFlow.value.filter { it.itemName != item.itemName }
        }
    }

    private fun addItem(item: SuggestedItem) {
        viewModelScope.launch(Dispatchers.Main) {
            if (itemsMutableFlow.value.any { it.itemName == item.itemName }) {
                itemNameTextFieldError = "Item already exists in the recipe"
            } else {
                itemsMutableFlow.value = itemsMutableFlow.value + item.toSingleItem()
                searchQueryMutableFlow.value = ""
            }
        }
    }

    private fun addItem() {
        viewModelScope.launch(Dispatchers.Main + defaultExceptionHandler { error ->
            when (error) {
                is ItemAlreadyOnListException -> itemNameTextFieldError = error.message
            }
        }) {
            val newItemName = searchQueryFlow.value
            val currentItems = itemsFlow.value
            if (newItemName.isBlank()) {
                throw IgnoredException()
            } else if (currentItems.any { it.itemName.lowercase() == newItemName.lowercase() }) {
                throw ItemAlreadyOnListException(getString(Res.string.error_item_already_in_recipe))
            }

            shoppingList.saveSearchResult(newItemName)
            itemsMutableFlow.value = currentItems + SingleItem(newItemName)
            searchQueryMutableFlow.value = ""
        }
    }

    private fun saveRecipe() {
        viewModelScope.launch(Dispatchers.Main + defaultExceptionHandler { error ->
            when (error) {
                is SQLIntegrityConstraintViolationException -> recipeNameTextFieldError =
                    error.message
                //todo other errors handling
            }
        }) {
            when (crud) {
                CRUD.INSERT -> repository.insertRecipe(
                    recipeName = recipeNameFlow.value,
                    items = itemsFlow.value
                )

                CRUD.UPDATE -> repository.updateRecipe(
                    recipe = editedRecipe!!.recipe, // Changed from editedRecipe!!.bundle
                    recipeName = recipeNameFlow.value,
                    items = itemsFlow.value
                )
            }
            emitSingleAction(RecipeSaved)
        }
    }
}
