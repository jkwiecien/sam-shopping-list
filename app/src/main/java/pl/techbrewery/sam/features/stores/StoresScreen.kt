package pl.techbrewery.sam.features.stores

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.R
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.ui.shared.PrimaryOutlineButton
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun StoresScreen(
    viewModel: StoresViewModel,
    modifier: Modifier = Modifier,
    onNavigationAction: (Any) -> Unit = {},
) {
    val stores by viewModel.stores.collectAsStateWithLifecycle()
    val onAction: (Any) -> Unit  = { action ->
        when (action) {
            is StorePressed, CreateStorePressed -> onNavigationAction(action)
            else -> viewModel.onAction(action)
        }
    }
    if (stores.isNotEmpty()) {
        StoresScreenContent(
            stores = stores,
            modifier = modifier,
            onAction = onAction
        )
    } else {
        EmptyStoresScreenContent(
            onAction = onAction
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoresScreenContent(
    stores: ImmutableList<Store>,
    modifier: Modifier = Modifier,
    onAction: (Any) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
            items(stores) { store ->
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            onAction(StorePressed(store))
                        }
                )
            }
            item {
                Text(
                    text = "Create store",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            onAction(CreateStorePressed)
                        }
                )
            }
        }
    }
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
                title = "Create store",
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
fun StoresScreenPreview() {
    SAMTheme {
        StoresScreenContent(
            stores = listOf(
                Store(name = "Biedronka"),
                Store(name = "Auchan", address = "ul. Puławska 123")
            ).toImmutableList(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStoresScreenContentPreview() {
    SAMTheme {
        EmptyStoresScreenContent()
    }

}
