package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.shared.HeterogeneousIcon
import pl.techbrewery.sam.shared.HeterogeneousVectorIcon
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun PrimaryFilledButton(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: HeterogeneousVectorIcon? = null,
    onPressed: () -> Unit = {},
) {
    Button(
        onClick = onPressed,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.Large)
        ) {
            if (leadingIcon != null) {
                HeterogeneousIcon(
                    icon = leadingIcon,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                SmallSpacingBox()
            }
            Text(
                text = title
            )
        }
    }
}


@Preview
@Composable
fun PrimaryButtonPreview() {
    SAMTheme {
        PrimaryFilledButton(
            title = "Save"
        )
    }
}

@Preview
@Composable
fun PrimaryButtonIconPreview() {
    SAMTheme {
        PrimaryFilledButton(
            title = "New shop",
            leadingIcon = HeterogeneousVectorIcon.VectorIcon(Icons.Filled.Add)
        )
    }
}

