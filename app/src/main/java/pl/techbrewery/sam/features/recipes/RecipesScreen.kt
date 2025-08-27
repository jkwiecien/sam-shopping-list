package pl.techbrewery.sam.features.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.R
import pl.techbrewery.sam.extensions.capitalize
// Assuming ItemBundleWithItems was in this package, RecipeWithItems should be too
// If not, this import might need adjustment based on where RecipeWithItems.kt actually is
import pl.techbrewery.sam.kmp.database.bundles.RecipeWithItems
import pl.techbrewery.sam.kmp.database.entity.Recipe
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.button_title_create_recipe
import pl.techbrewery.sam.resources.label_no_recipes_message
import pl.techbrewery.sam.resources.label_no_recipes_title
import pl.techbrewery.sam.ui.shared.LargeSpacingBox
import pl.techbrewery.sam.ui.shared.PrimaryOutlinedButton
import pl.techbrewery.sam.ui.shared.SmallSpacingBox
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.SwipeToDismissBackground
import pl.techbrewery.sam.ui.shared.rememberSwipeToDeleteBoxState
import pl.techbrewery.sam.ui.shared.stringResourceCompat
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel,
    onExternalAction: (Any) -> Unit = {},
) {
    val onAction: (Any) -> Unit = { action ->
        when (action) {
            is CreateRecipePressed, is RecipePressed -> onExternalAction(action)
            else -> viewModel.onAction(action)
        }
    }
    val recipes by viewModel.recipesFlow.collectAsStateWithLifecycle()

    if (recipes.isNotEmpty()) {
        RecipesScreenContent(
            recipes = recipes,
            onAction = onAction
        )
    } else {
        EmptyRecipesScreenContent(
            onAction = onAction
        )
    }
}

@Composable
private fun RecipesScreenContent(
    recipes: ImmutableList<RecipeWithItems>, // Changed from ItemBundleWithItems
    onAction: (Any) -> Unit = {}
) {
    Surface {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            items(recipes, key = { it.recipe.recipeId }) { recipeWithItems -> // Changed from it.bundle.bundleId, param recipe to recipeWithItems
                val dismissState =
                    rememberSwipeToDeleteBoxState { onAction(RecipeDismissed(recipeWithItems)) } // param recipe to recipeWithItems
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        SwipeToDismissBackground(dismissState.dismissDirection)
                    }
                ) {
                    RecipeItem(
                        name = recipeWithItems.recipe.name, // Changed from recipe.bundle.name
                        modifier = Modifier
                            .animateItem()
                            .clickable {
                                onAction(RecipePressed(recipeWithItems)) // param recipe to recipeWithItems
                            }

                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RecipesScreenContentPreview() {
    SAMTheme {
        val recipesData = listOf(
            Recipe(name = "Recipe 1", recipeId = 1), // Changed bundleId to recipeId
            Recipe(name = "Recipe 2", recipeId = 2)  // Changed bundleId to recipeId
        ).map {
            RecipeWithItems( // Changed from ItemBundleWithItems
                recipe = it, // Changed from bundle = it
                items = emptyList()
            )
        }
        RecipesScreenContent(
            recipes = recipesData.toImmutableList(),
            onAction = {}
        )
    }
}

@Composable
private fun RecipeItem(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_recipe_24dp),
            contentDescription = name,
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
            text = name.capitalize(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmptyRecipesScreenContent(
    onAction: (Any) -> Unit = {}
) {
    Surface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.Large)
        ) {
            Image(
                painter = painterResource(R.drawable.illustration_empty_recipes),
                contentDescription = "Empty recipes illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
                    .height(200.dp)
            )
            LargeSpacingBox()
            Text(
                text = stringResourceCompat(
                    Res.string.label_no_recipes_title,
                    "No recipes yet"
                ),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            SmallSpacingBox()
            Text(
                text = stringResourceCompat(
                    Res.string.label_no_recipes_message,
                    "Create your first recipe to start planning your meals."
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            LargeSpacingBox()
            PrimaryOutlinedButton(
                title = stringResourceCompat(
                    Res.string.button_title_create_recipe,
                    "Create recipe"
                ),
                onPressed = { onAction(CreateRecipePressed) }
            )
        }
    }
}

@Preview
@Composable
private fun EmptyRecipesScreenContentPreview() {
    SAMTheme {
        EmptyRecipesScreenContent()
    }
}
