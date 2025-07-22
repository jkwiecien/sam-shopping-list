package pl.techbrewery.sam.features.navigation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.jetbrains.compose.resources.stringResource
import pl.techbrewery.sam.features.navigation.NavigationTopLevelRoutes
import pl.techbrewery.sam.shared.HeterogeneousIcon

@SuppressLint("RestrictedApi")
@Composable
internal fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val itemColors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f), // Example: Use surfaceVariant
            selectedIconColor = MaterialTheme.colorScheme.outline,
            selectedTextColor = MaterialTheme.colorScheme.outline,
            unselectedIconColor = MaterialTheme.colorScheme.tertiaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.tertiaryContainer
        )



        NavigationTopLevelRoutes.routes.forEach { topLevelRoute ->
            val selected = currentDestination?.hierarchy?.any { destination ->
                destination.route?.startsWith(topLevelRoute.route) == true
            } == true

            val tabTitle = stringResource(topLevelRoute.titleResource)
            val iconTint = if (selected) {
                itemColors.selectedIconColor
            } else {
                itemColors.unselectedIconColor
            }

            NavigationBarItem(
                icon = {
                    HeterogeneousIcon(
                        topLevelRoute.icon,
                        contentDescription = tabTitle,
                        tint = iconTint,
                        modifier = Modifier.Companion.size(24.dp)
                    )
                },
                label = { Text(tabTitle) },
                selected = selected,
                colors = itemColors,
                onClick = {
                    navController.navigate(topLevelRoute.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}