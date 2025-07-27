package pl.techbrewery.sam.features.shoppinglist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.R
import pl.techbrewery.sam.extensions.capitalize
import pl.techbrewery.sam.kmp.model.SuggestedItem
import pl.techbrewery.sam.kmp.model.SuggestedItemType
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.label_add_item
import pl.techbrewery.sam.ui.shared.DropdownItem
import pl.techbrewery.sam.ui.shared.PrimaryTextField
import pl.techbrewery.sam.ui.shared.SmallSpacingBox
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.primaryTextFieldColors
import pl.techbrewery.sam.ui.shared.stringResourceCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemTextField(
    value: String,
    suggestedItems: List<DropdownItem<SuggestedItem>>,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    errorText: String? = null,
    onValueChange: (String) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    onSelectedItemChanged: (SuggestedItem) -> Unit = {},
    onDeleteSuggestedItemPressed: (SuggestedItem) -> Unit = {},
    onDonePressed: () -> Unit = {}
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { },
        modifier = modifier
    ) {
        PrimaryTextField(
            value = value,
            label = errorText ?: stringResourceCompat(Res.string.label_add_item, "Add item"),
            errorText = errorText,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search or Add Item") },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") }
                    ) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "Clear query",
                        )
                    }
                }
            },
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            onDonePressed = onDonePressed,
            colors = primaryTextFieldColors(),
            modifier = Modifier
                .onFocusChanged { focusState -> onFocusChanged(focusState.hasFocus) }
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { },
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = Spacing.Small)
        ) {
            suggestedItems.forEach { dropdownItem ->
                val item = dropdownItem.item
                SuggestedItem(
                    name = item.itemName,
                    type = item.type,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onSelectedItemChanged(item)
                        }
                        .padding(vertical = Spacing.Tiny / 2, horizontal = Spacing.Small),
                    onDeletePressed = { onDeleteSuggestedItemPressed(item) }
                )
            }
        }
    }
}

@Composable
private fun SuggestedItem(
    name: String,
    type: SuggestedItemType,
    modifier: Modifier = Modifier,
    onDeletePressed: () -> Unit = {}
) {
    val iconResId = when (type) {
        SuggestedItemType.ITEM -> R.drawable.ic_grocery_24dp
        SuggestedItemType.RECIPE -> R.drawable.ic_recipe_24dp
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = name.capitalize(),
            modifier = Modifier
                .size(32.dp)
                .background(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                .padding(6.dp)
        )
        SmallSpacingBox()
        Text(
            text = name.capitalize(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDeletePressed
        ) {
            Icon(
                imageVector = Icons.Outlined.RemoveCircleOutline,
                contentDescription = "Remove item"
            )
        }
    }
}

@Preview
@Composable
fun SuggestedSingleItemPreview() {
    SuggestedItem(
        name = "Apple",
        type = SuggestedItemType.ITEM
    )
}

@Preview
@Composable
fun SuggestedRecipePreview() {
    SuggestedItem(
        name = "Pasta Carbonara",
        type = SuggestedItemType.RECIPE
    )
}
