package pl.techbrewery.sam.features.recipes.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.R
import pl.techbrewery.sam.extensions.capitalize
import pl.techbrewery.sam.extensions.closeKeyboardOnPress
import pl.techbrewery.sam.features.shoppinglist.SuggestedItemSelected
import pl.techbrewery.sam.features.shoppinglist.ui.ItemTextField
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.action_save
import pl.techbrewery.sam.resources.label_ingredients
import pl.techbrewery.sam.resources.label_recipe_name
import pl.techbrewery.sam.shared.SearchQueryChanged
import pl.techbrewery.sam.ui.shared.DropdownItem
import pl.techbrewery.sam.ui.shared.PrimaryFilledButton
import pl.techbrewery.sam.ui.shared.PrimaryTextField
import pl.techbrewery.sam.ui.shared.SmallSpacingBox
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.SwipeToDismissBackground
import pl.techbrewery.sam.ui.shared.rememberSwipeToDeleteBoxState
import pl.techbrewery.sam.ui.shared.stringResourceCompat
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun RecipeEditorScreen(
    viewModel: RecipeEditorViewModel,
    onExternalAction: (Any) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.actionsFlow.collect { action ->
            onExternalAction(action)
        }
    }

    RecipeEditorScreenContent(
        recipeName = viewModel.recipeNameFlow.collectAsStateWithLifecycle().value,
        items = viewModel.itemsFlow.collectAsStateWithLifecycle().value,
        suggestedItems = viewModel.suggestedItemsDropdownItems.collectAsStateWithLifecycle().value,
        searchQuery = viewModel.searchQueryFlow.collectAsStateWithLifecycle().value,
        saveButtonEnabled = viewModel.saveButtonEnabledFlow.collectAsStateWithLifecycle().value,
        recipeNameTextFieldError = viewModel.recipeNameTextFieldError,
        itemNameTextFieldError = viewModel.itemNameTextFieldError,
        onAction = { viewModel.onAction(it) }
    )
}

@Composable
private fun RecipeEditorScreenContent(
    recipeName: String,
    items: ImmutableList<SingleItem> = emptyList<SingleItem>().toImmutableList(),
    suggestedItems: ImmutableList<DropdownItem<SingleItem>>,
    searchQuery: String = "",
    recipeNameTextFieldError: String? = null,
    itemNameTextFieldError: String? = null,
    saveButtonEnabled: Boolean = false,
    onAction: (Any) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.Large)
                .closeKeyboardOnPress(
                    onPressedSomething = { focusManager.clearFocus() }
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large)
        ) {
            PrimaryTextField(
                value = recipeName,
                label = stringResourceCompat(Res.string.label_recipe_name, "Recipe name"),
                errorText = recipeNameTextFieldError,
                onValueChange = { onAction(RecipeNameChanged(it)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                onDonePressed = {
                    focusManager.clearFocus()
                },
                modifier = Modifier.fillMaxWidth()
            )
            ItemTextField(
                value = searchQuery,
                expanded = suggestedItems.isNotEmpty(),
                suggestedItems = suggestedItems,
                errorText = itemNameTextFieldError,
                onValueChange = { onAction(SearchQueryChanged(it)) },
                onDonePressed = { onAction(IngredientNameFieldKeyboardDonePressed) },
                onSelectedItemChanged = { onAction(SuggestedItemSelected(it)) },
                modifier = Modifier.fillMaxWidth()
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResourceCompat(Res.string.label_ingredients, "Ingredients"),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.Small)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = Spacing.Small),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    items(items, key = { it.itemName }) { item ->
                        val dismissState =
                            rememberSwipeToDeleteBoxState { onAction(RecipeItemDismissed(item)) }
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                SwipeToDismissBackground(dismissState.dismissDirection)
                            }
                        ) {
                            IngredientItem(
                                itemName = item.itemName,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
            PrimaryFilledButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResourceCompat(Res.string.action_save, "Save"),
                enabled = saveButtonEnabled,
                onPressed = { onAction(SaveRecipePressed) }
            )
        }
    }
}

@Composable
private fun IngredientItem(
    itemName: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_grocery_24dp),
            contentDescription = itemName,
            modifier = Modifier
                .size(48.dp)
                .background(
                    shape = RoundedCornerShape(Spacing.Small),
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                .padding(Spacing.Medium)
        )
        SmallSpacingBox()
        Text(
            text = itemName.capitalize(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeEditorScreenContentPreview() {
    val items = listOf(
        "pasta", "butter", "cheese", "beacon", "black pepper"
    ).mapIndexed { index, itemName ->
        SingleItem(
            itemName = itemName
        )
    }.toImmutableList()

    SAMTheme { // It's a good practice to wrap previews in your app's theme
        RecipeEditorScreenContent(
            recipeName = "Delicious Pasta",
            items = items,
            suggestedItems = emptyList<DropdownItem<SingleItem>>().toImmutableList(),
            searchQuery = "Pot",
            itemNameTextFieldError = null,
        )
    }
}
