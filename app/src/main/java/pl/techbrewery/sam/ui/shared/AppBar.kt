package pl.techbrewery.sam.ui.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String
) {
    TopAppBar(
        title = { Text(text = title) },
//        actions = {
//            IconButton(onClick = { /* Handle add item to top bar action */ }) {
//                Icon(Icons.Filled.Add, contentDescription = "Add Item")
//            }
//        }
    )
}