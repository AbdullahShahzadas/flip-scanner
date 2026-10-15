package com.flipscanner.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipscanner.app.data.local.InventoryItem
import com.flipscanner.app.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardStats(
    val totalItems: Int = 0,
    val unsoldCount: Int = 0,
    val soldCount: Int = 0,
    val totalSpent: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: InventoryRepository
) : ViewModel() {

    val stats: StateFlow<DashboardStats> = repository.allItems
        .map { items -> items.toStats() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardStats()
        )

    private fun List<InventoryItem>.toStats(): DashboardStats {
        val sold = filter { it.isSold }
        return DashboardStats(
            totalItems = size,
            unsoldCount = count { !it.isSold },
            soldCount = sold.size,
            totalSpent = sumOf { it.buyPrice },
            totalRevenue = sold.sumOf { it.soldPrice ?: 0.0 },
            totalProfit = sold.sumOf { (it.soldPrice ?: 0.0) - it.buyPrice }
        )
    }
}
