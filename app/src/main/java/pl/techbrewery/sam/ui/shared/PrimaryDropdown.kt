package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.kmp.utils.tempLog
import pl.techbrewery.sam.ui.theme.SAMTheme

data class DropdownItem<T>(
    val item: T,
    val text: String = "",
    val selectedText: String = text,
    val extraText: String? = null
) {

    companion object {
        fun <T> dummyItem(item: T): DropdownItem<T> = DropdownItem(item)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PrimaryDropdown(
    selectedItem: DropdownItem<T>,
    dropdownItems: List<DropdownItem<T>>,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    initiallyExpanded: Boolean = false,
    colors: TextFieldColors = primaryDropdownColors(),
    leadingIcon: @Composable (() -> Unit)? = null,
    createItem: @Composable ((DropdownItem<T>) -> Unit)? = null,
    onSelectedItemChanged: (DropdownItem<T>) -> Unit = {},
) {
    tempLog("selected item: $selectedItem")
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val trailingIcon: @Composable (() -> Unit) = {
        ExposedDropdownMenuDefaults.TrailingIcon(
            expanded = expanded
        )
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        PrimaryTextField(
            readOnly = true,
            enabled = enabled,
            value = selectedItem.selectedText,
            label = label,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            colors = colors,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
        ) {
            dropdownItems.forEach { item ->
                val onPressed = {
                    onSelectedItemChanged(item)
                    expanded = false
                }
                if (createItem != null) {
                    Box(
                        modifier = Modifier
                            .padding(
                                vertical = Spacing.Tiny, horizontal = Spacing.Small
                            )
                            .clickable(onClick = onPressed)
                    ) {
                        createItem(item)
                    }
                } else {
                    DropdownMenuItem(
                        text = { Text(text = item.text) },
                        onClick = onPressed
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryDropdownItem() {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun primaryDropdownColors() = ExposedDropdownMenuDefaults.textFieldColors(
    focusedIndicatorColor = Transparent,
    disabledIndicatorColor = Transparent,
    unfocusedIndicatorColor = Transparent,
    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
)

@Preview
@Composable
private fun PrimaryDropdownPreview() {
    val items = listOf(
        DropdownItem(item = "O1", text = "Option 1"),
    )
    SAMTheme {
        PrimaryDropdown(
            selectedItem = items.first(),
            dropdownItems = items
        )
    }
}