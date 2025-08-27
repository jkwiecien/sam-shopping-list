package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.R
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun GoogleSignInButton(
    onPressed: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFFF2F2F2))
            .padding(Spacing.Small)
            .padding(horizontal = Spacing.Large)
            .clickable(onClick = onPressed)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_google_g_48dp),
            contentDescription = "Google logo",
            modifier = Modifier.size(24.dp)
        )
        SmallSpacingBox()
        Text(
            text = "Sign in with Google",
            color = Black,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview
@Composable
private fun GoogleSignInButtonPreview() {
    SAMTheme { GoogleSignInButton() }
}