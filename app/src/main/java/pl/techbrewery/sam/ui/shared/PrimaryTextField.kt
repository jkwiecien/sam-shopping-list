package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryTextField(
    value: String,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit = {},
    onDonePressed: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = supportingText?.let { { Text(it) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = { onDonePressed() }
        ),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Transparent,
            disabledIndicatorColor = Transparent,
            unfocusedIndicatorColor = Transparent,
            focusedSupportingTextColor = MaterialTheme.colorScheme.tertiary,
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
        ),

        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Search or Add Item")
        }
    )
}