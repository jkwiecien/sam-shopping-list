package pl.techbrewery.sam.features.stores

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.techbrewery.sam.R
import pl.techbrewery.sam.ui.shared.PrimaryOutlineButton
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun EmptyStoresScreen(
    onAction: (Any) -> Unit = {},
) {
    EmptyStoresScreenContent(
        onAction = onAction
    )
}

@Composable
fun EmptyStoresScreenContent(
    onAction: (Any) -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.illustration_empty_stores), // Replace with your actual drawable
                contentDescription = "No stores yet",
                contentScale = ContentScale.Crop, // Crop to fill bounds
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .height(250.dp) // Adjust height as needed
            )

            Spacer(modifier = Modifier.height(Spacing.XL))

            Text(
                text = "No stores yet",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(Spacing.Small))

            Text(
                text = "Create a shop to start adding items to your shopping list.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp), // Add some horizontal padding for centering
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryOutlineButton(
                title = "Create shop",
                onPressed = { onAction(CreateStorePressed) },
                modifier = Modifier
                    .padding(horizontal = 40.dp) // Add padding to make button wider
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStoresScreenContentPreview() {
    SAMTheme {
        EmptyStoresScreenContent()
    }

}
