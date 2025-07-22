package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun PrimaryTextField(
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
    onDonePressed: () -> Unit = {},
) {
    TextField(
        value = value,
        modifier = modifier,
        onValueChange = onValueChange,
        label = supportingText?.let { { Text(
            text = supportingText,
            modifier = Modifier.fillMaxWidth()) } },
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
        leadingIcon = leadingIcon
    )
}

@Preview
@Composable
fun PrimaryTextFieldPreview() {
    SAMTheme {
        PrimaryTextField(value = "Some value")
    }
}
