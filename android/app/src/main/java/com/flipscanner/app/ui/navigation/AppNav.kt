package com.flipscanner.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flipscanner.app.ui.dashboard.DashboardScreen
import com.flipscanner.app.ui.home.HomeScreen
import com.flipscanner.app.ui.inventory.InventoryScreen
import com.flipscanner.app.ui.listing.ListingScreen
import com.flipscanner.app.ui.result.ResultScreen
import com.flipscanner.app.ui.scan.ScanScreen
import com.flipscanner.app.ui.subscription.SubscriptionScreen

object Routes {
    const val HOME = "home"
    const val SCAN = "scan"
    const val RESULT = "result"
    const val LISTING = "listing"
    const val INVENTORY = "inventory"
    const val DASHBOARD = "dashboard"
    const val SUBSCRIPTION = "subscription"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onScan = { nav.navigate(Routes.SCAN) },
                onInventory = { nav.navigate(Routes.INVENTORY) },
                onDashboard = { nav.navigate(Routes.DASHBOARD) },
                onSubscribe = { nav.navigate(Routes.SUBSCRIPTION) }
            )
        }
        composable(Routes.SCAN) {
            ScanScreen(
                onBack = { nav.popBackStack() },
                onDone = { nav.navigate(Routes.RESULT) }
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                onBack = { nav.popBackStack() },
                onGenerateListing = { nav.navigate(Routes.LISTING) }
            )
        }
        composable(Routes.LISTING) {
            ListingScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.INVENTORY) {
            InventoryScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.SUBSCRIPTION) {
            SubscriptionScreen(onBack = { nav.popBackStack() })
        }
    }
}
