package pl.techbrewery.sam.features.stores

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.techbrewery.sam.kmp.database.entity.Store
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun StoresScreen(
    viewModel: StoresViewModel,
    modifier: Modifier = Modifier,
    onNavigationAction: (Any) -> Unit = {},
) {
        val stores by viewModel.stores.collectAsStateWithLifecycle()
        StoresScreenContent(
            stores = stores,
            modifier = modifier,
            onAction = { action ->
                when (action) {
                    is StorePressed, CreateStorePressed -> onNavigationAction(action)
                    else -> viewModel.onAction(action)
                }
            }
        )
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
