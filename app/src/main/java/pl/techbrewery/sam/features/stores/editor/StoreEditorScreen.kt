package pl.techbrewery.sam.features.stores.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.techbrewery.sam.extensions.closeKeyboardOnPress
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.action_save
import pl.techbrewery.sam.ui.shared.LargeSpacingBox
import pl.techbrewery.sam.ui.shared.PrimaryFilledButton
import pl.techbrewery.sam.ui.shared.PrimaryTextField
import pl.techbrewery.sam.ui.shared.Spacing
import pl.techbrewery.sam.ui.shared.stringResourceCompat
import pl.techbrewery.sam.ui.theme.SAMTheme

@Composable
fun StoreEditorScreen(
    viewModel: StoreEditorViewModel,
    onExternalAction: (Any) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.actionsFlow.collect { action ->
            onExternalAction(action)
        }
    }

    StoreEditorScreenContent(
        storeName = viewModel.storeName,
        storeAddress = viewModel.storeAddress,
        onAction = { viewModel.onAction(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreEditorScreenContent(
    storeName: String,
    modifier: Modifier = Modifier,
    storeAddress: String? = null,
    onAction: (Any) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        val focusManager = LocalFocusManager.current
        Column(
            modifier = modifier
                .padding(Spacing.Large)
                .closeKeyboardOnPress(
                    onPressedSomething = { focusManager.clearFocus() }
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large)
        ) {
            PrimaryTextField(
                value = storeName,
                label = "Store name",
                onValueChange = { onAction(StoreNameChanged(it)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                onDonePressed = { focusManager.clearFocus() },
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryTextField(
                value = storeAddress ?: "",
                label = "Store address (optional)",
                onValueChange = { onAction(StoreAddressChanged(it)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                onDonePressed = { focusManager.clearFocus() },
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryFilledButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResourceCompat(Res.string.action_save, "Save"),
                onPressed = { onAction(SaveStorePressed) }
            )
        }
    }
}

@Composable
fun CategoryItem(
    categoryName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = categoryName, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun StoresScreenPreview() {
    SAMTheme { // Wrap with your app's theme if you have one
        StoreEditorScreenContent(
            storeName = "Lidl",
        )
    }
}