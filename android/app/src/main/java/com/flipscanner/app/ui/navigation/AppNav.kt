package com.flipscanner.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flipscanner.app.ui.dashboard.DashboardScreen
import com.flipscanner.app.ui.home.HomeScreen
import com.flipscanner.app.ui.inventory.InventoryScreen
import com.flipscanner.app.ui.result.ResultScreen
import com.flipscanner.app.ui.scan.ScanScreen

object Routes {
    const val HOME = "home"
    const val SCAN = "scan"
    const val RESULT = "result"
    const val INVENTORY = "inventory"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onScan = { nav.navigate(Routes.SCAN) },
                onInventory = { nav.navigate(Routes.INVENTORY) },
                onDashboard = { nav.navigate(Routes.DASHBOARD) }
            )
        }
        composable(Routes.SCAN) {
            ScanScreen(
                onBack = { nav.popBackStack() },
                onDone = { nav.navigate(Routes.RESULT) }
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.INVENTORY) {
            InventoryScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(onBack = { nav.popBackStack() })
        }
    }
}
