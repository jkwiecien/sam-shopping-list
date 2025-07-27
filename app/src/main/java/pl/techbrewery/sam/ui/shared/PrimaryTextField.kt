package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
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
    label: String? = null,
    errorText: String? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    colors: TextFieldColors = primaryTextFieldColors(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
    onDonePressed: () -> Unit = {},
) {
    TextField(
        value = value,
        modifier = modifier,
        readOnly = readOnly,
        enabled = enabled,
        isError = errorText != null,
        onValueChange = onValueChange,
        label = label?.let {
            {
                Text(
                    text = errorText ?: label,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = { onDonePressed() }
        ),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
    )
}

@Composable
fun primaryTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = Transparent,
    disabledIndicatorColor = Transparent,
    unfocusedIndicatorColor = Transparent,
    focusedSupportingTextColor = MaterialTheme.colorScheme.tertiary,
    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    errorSupportingTextColor = MaterialTheme.colorScheme.error,
    errorContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    errorIndicatorColor = Transparent,
    errorLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
    errorTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
)

@Preview
@Composable
private fun PrimaryTextFieldPreview() {
    SAMTheme {
        PrimaryTextField(value = "Some value")
    }
}
