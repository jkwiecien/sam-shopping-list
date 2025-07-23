package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun PrimaryOutlinedButton(
    title: String,
    modifier: Modifier = Modifier,
    onPressed: () -> Unit = {},
) {
    OutlinedButton (
        onClick = onPressed,
        modifier = modifier
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(
                horizontal = Spacing.Large
            )
        )
    }
}

@Preview
@Composable
fun PrimaryOutlineButtonPreview() {
    SAMTheme {
        PrimaryOutlinedButton(
            title = "Save"
        )
    }
}

