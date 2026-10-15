package com.flipscanner.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flipscanner.app.data.local.InventoryItem
import com.flipscanner.app.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    val items: StateFlow<List<InventoryItem>> = repository.allItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun markAsSold(id: Long, soldPrice: Double) {
        viewModelScope.launch {
            repository.markAsSold(id, soldPrice)
        }
    }

    fun markAsUnsold(id: Long) {
        viewModelScope.launch {
            repository.markAsUnsold(id)
        }
    }

    fun delete(item: InventoryItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}
