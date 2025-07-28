package pl.techbrewery.sam.features.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.R
import pl.techbrewery.sam.features.recipes.editor.CreateRecipePressed
import pl.techbrewery.sam.features.recipes.editor.RecipeEditorViewModel
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.button_title_create_recipe
import pl.techbrewery.sam.resources.label_no_recipes_message
import pl.techbrewery.sam.resources.label_no_recipes_title
import pl.techbrewery.sam.ui.shared.LargeSpacingBox
import pl.techbrewery.sam.ui.shared.PrimaryOutlinedButton
import pl.techbrewery.sam.ui.shared.SmallSpacingBox
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.stringResourceCompat
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun RecipesScreen(
    viewModel: RecipeEditorViewModel
) {
    val onAction: (Any) -> Unit = { viewModel.onAction(it) }
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
