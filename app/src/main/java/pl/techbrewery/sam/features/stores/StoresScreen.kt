package pl.techbrewery.sam.features.stores

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import pl.techbrewery.sam.ui.shared.LargeSpacingBox
import pl.techbrewery.sam.ui.shared.PrimaryOutlinedButton
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun StoresScreen(
    viewModel: StoresViewModel,
    modifier: Modifier = Modifier,
    onExternalAction: (Any) -> Unit = {},
) {
    val stores by viewModel.stores.collectAsStateWithLifecycle()
    val onAction: (Any) -> Unit = { action ->
        when (action) {
            is StorePressed, CreateStorePressed -> onExternalAction(action)
            else -> viewModel.onAction(action)
        }
    }
    StoresScreenContent(
        stores = stores,
        modifier = modifier,
        onAction = onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoresScreenContent(
    stores: ImmutableList<Store>,
    modifier: Modifier = Modifier,
    onAction: (Any) -> Unit = {}
) {
    Surface {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(Spacing.Large)
        ) {
            items(stores, key = { store -> store.storeId }) { store ->
                StoreItem(
                    store = store,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
fun StoreItem(
    store: Store,
    onAction: ((Any) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (onAction != null) {
                    it.clickable { onAction(StorePressed(store)) }
                } else {
                    it
                }
            }
            .padding(vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconShape = RoundedCornerShape(Spacing.Small)
        Icon(
            painter = painterResource(R.drawable.ic_store_24dp),
            contentDescription = store.name,
            modifier = Modifier
                .size(48.dp)
                .background(
                    shape = iconShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                .let {
                    if (store.selected) {
                        it.border(
                            width = 2.dp,
                            shape = RoundedCornerShape(Spacing.Small),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        it
                    }
                }
                .padding(Spacing.Medium)
        )
        LargeSpacingBox()
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (store.address.isNotEmpty()) {
                Text(
                    text = store.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
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
                Store(storeId = 0, name = "Biedronka", selected = true),
                Store(storeId = 1, name = "Auchan", address = "ul. Puławska 123")
            ).toImmutableList(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

