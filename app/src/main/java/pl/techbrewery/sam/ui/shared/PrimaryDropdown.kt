package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.fillMaxWidth
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
    items: List<DropdownItem<T>>,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    colors: TextFieldColors = primaryDropdownColors(),
    leadingIcon: @Composable (() -> Unit)? = null,

    onSelectedItemChanged: (DropdownItem<T>) -> Unit = {},
) {
    tempLog("selected item: $selectedItem")
    var expanded by remember { mutableStateOf(false) }
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
            }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item.text) },
                    onClick = {
                        onSelectedItemChanged(item)
                        expanded = false
                    }
                )
            }
        }
    }
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
        DropdownItem(item = "O2", text = "Option 2"),
        DropdownItem(item = "O3", text = "Option 3"),
    )
    SAMTheme {
        PrimaryDropdown(
            selectedItem = items.first(),
            items = items
        )
    }
}