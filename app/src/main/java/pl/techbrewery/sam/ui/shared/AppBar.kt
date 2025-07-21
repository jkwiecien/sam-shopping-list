package pl.techbrewery.sam.ui.shared

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import pl.techbrewery.sam.resources.Res
import pl.techbrewery.sam.resources.action_up

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    canNavigateBack: Boolean = false,
    navigateUp: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background // Use background from theme
            // You might also want to set other colors like titleContentColor
            // titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.action_up)
                    )
                }
            }
        }
//        actions = {
//            IconButton(onClick = { /* Handle add item to top bar action */ }) {
//                Icon(Icons.Filled.Add, contentDescription = "Add Item")
//            }
//        }
    )
}