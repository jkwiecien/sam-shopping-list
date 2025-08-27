package pl.techbrewery.sam.features.auth

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.techbrewery.sam.R
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.label_auth_rationale
import pl.techbrewery.sam.ui.shared.GoogleSignInButton
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.stringResourceCompat
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun AuthScreen(
    onAction: (Any) -> Unit = {}
) {
    Surface {
        Column {
            Image(
                painter = painterResource(R.drawable.illustration_auth),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(Spacing.XL)
            ) {
                Text(
                    text = stringResourceCompat(
                        Res.string.label_auth_rationale,
                        "To make sure you never lose your data we require you to sign in"
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                GoogleSignInButton(onPressed = { onAction(GoogleSignInPressed) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultAuthScreenPreview() {
    SAMTheme {
        AuthScreen()
    }
}