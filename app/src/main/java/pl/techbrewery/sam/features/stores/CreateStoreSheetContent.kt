package pl.techbrewery.sam.features.stores

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.techbrewery.sam.R // Assuming your image is in res/drawable

@Composable
fun CreateStoreSheetContent() {
    Column(
        modifier = Modifier
            .background(Color(0xFF1E2D1E)) // Dark green background
    ) {
        // Image Section
        Image(
            painter = painterResource(id = R.drawable.store_welcome), // Replace with your image
            contentDescription = "Shopping illustration",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop // Adjust as needed
        )

        // Text and Button Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Shop Smarter with Optimized Lists",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "To automatically order items in your shopping list, we need to know your shop's layout. This ensures a seamless and efficient shopping experience.",
                color = Color.LightGray, // Lighter text color for description
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(
                onClick = { /* Handle button click */ },
                shape = RoundedCornerShape(50), // Rounded corners
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8BC34A) // Light green button
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Set a fixed height for the button
            ) {
                Text(
                    text = "Set Up Shop Layout",
                    color = Color.Black, // Dark text on light green button
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateStoreSheetContentPreview() {
    MaterialTheme { // It's good practice to wrap previews in your app's theme
        CreateStoreSheetContent()
    }
}
