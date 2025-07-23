package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun SearchField(
    query: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit = {},
    onDonePressed: () -> Unit = {},
) {
    PrimaryTextField(
        value = query,
        modifier = modifier,
        label = supportingText,
        keyboardOptions = keyboardOptions,
        onValueChange = onValueChange,
        onDonePressed = onDonePressed,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search or Add Item") }
    )
}

@Preview
@Composable
private fun SearchFieldPreview() {
    SAMTheme {
        SearchField(query = "Query")
    }
}