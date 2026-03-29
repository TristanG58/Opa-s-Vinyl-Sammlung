package com.opasvinyl.sammlung.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.opasvinyl.sammlung.navigation.BottomNavItem
import com.opasvinyl.sammlung.ui.theme.VinylBrown
import com.opasvinyl.sammlung.ui.theme.VinylCream
import com.opasvinyl.sammlung.ui.theme.VinylGold

@Composable
fun BottomNavBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem.Collection,
        BottomNavItem.Wishlist,
        BottomNavItem.Stats
    )

    NavigationBar(
        containerColor = VinylBrown,
        contentColor = VinylCream
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(BottomNavItem.Collection.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VinylGold,
                    selectedTextColor = VinylGold,
                    unselectedIconColor = VinylCream,
                    unselectedTextColor = VinylCream,
                    indicatorColor = VinylBrown
                )
            )
        }
    }
}
