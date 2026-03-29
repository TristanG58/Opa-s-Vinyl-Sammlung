package com.opasvinyl.sammlung.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.opasvinyl.sammlung.ui.screens.add.AddRecordScreen
import com.opasvinyl.sammlung.ui.screens.detail.DetailScreen
import com.opasvinyl.sammlung.ui.screens.library.LibraryScreen
import com.opasvinyl.sammlung.ui.screens.search.SearchScreen
import com.opasvinyl.sammlung.ui.screens.stats.StatsScreen
import com.opasvinyl.sammlung.ui.screens.wishlist.WishlistScreen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Collection : BottomNavItem("collection", "Sammlung", Icons.Default.Album)
    data object Wishlist : BottomNavItem("wishlist", "Wunschliste", Icons.Default.FavoriteBorder)
    data object Stats : BottomNavItem("stats", "Statistik", Icons.Default.BarChart)
}

object Routes {
    const val DETAIL = "detail/{recordId}"
    const val ADD = "add"
    const val SEARCH = "search"
    const val SEARCH_BARCODE = "search?barcode={barcode}"

    fun detail(recordId: Long) = "detail/$recordId"
    fun searchWithBarcode(barcode: String) = "search?barcode=$barcode"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Collection.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Collection.route) {
            LibraryScreen(
                onRecordClick = { id -> navController.navigate(Routes.detail(id)) },
                onAddClick = { navController.navigate(Routes.ADD) }
            )
        }

        composable(BottomNavItem.Wishlist.route) {
            WishlistScreen(
                onRecordClick = { id -> navController.navigate(Routes.detail(id)) },
                onAddClick = { navController.navigate(Routes.ADD) }
            )
        }

        composable(BottomNavItem.Stats.route) {
            StatsScreen()
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId") ?: return@composable
            DetailScreen(
                recordId = recordId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD) {
            AddRecordScreen(
                onBack = { navController.popBackStack() },
                onSearchDiscogs = { navController.navigate(Routes.SEARCH) },
                onRecordAdded = { id ->
                    navController.popBackStack()
                    navController.navigate(Routes.detail(id))
                }
            )
        }

        composable(
            route = "search?barcode={barcode}",
            arguments = listOf(navArgument("barcode") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
            SearchScreen(
                initialBarcode = barcode,
                onBack = { navController.popBackStack() },
                onRecordSelected = { id ->
                    navController.popBackStack(Routes.ADD, inclusive = true)
                    navController.navigate(Routes.detail(id))
                }
            )
        }
    }
}
