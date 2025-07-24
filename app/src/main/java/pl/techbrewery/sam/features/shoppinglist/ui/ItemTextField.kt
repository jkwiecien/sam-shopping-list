package pl.techbrewery.sam.features.shoppinglist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.kmp.database.entity.SingleItem
import pl.techbrewery.sam.ui.shared.DropdownItem
import pl.techbrewery.sam.ui.shared.PrimaryTextField
import pl.techbrewery.sam.ui.shared.primaryTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  ItemTextField(
    value: String,
    suggestedItems: List<DropdownItem<SingleItem>>,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    itemTextFieldError: String? = null,
    onValueChange: (String) -> Unit = {},
    onSelectedItemChanged: (SingleItem) -> Unit = {},
    onDonePressed: () -> Unit = {}
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { },
        modifier = modifier
    ) {
        PrimaryTextField(
            value = value,
            label = itemTextFieldError ?: "Add item",
            isError = itemTextFieldError != null,
            leadingIcon =  { Icon(Icons.Filled.Search, contentDescription = "Search or Add Item") },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = {onValueChange("")}
                    ) {
                        Icon(Icons.Filled.Clear,
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
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {  },
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
        ) {
            suggestedItems.forEach { dropdownItem ->
                DropdownMenuItem(
                    text = { Text(text = dropdownItem.text) },
                    onClick = {
                        onSelectedItemChanged(dropdownItem.item)
                    }
                )

            }
        }
    }
}