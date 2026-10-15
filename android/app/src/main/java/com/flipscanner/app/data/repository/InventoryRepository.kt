package com.flipscanner.app.data.repository

import com.flipscanner.app.data.local.InventoryDao
import com.flipscanner.app.data.local.InventoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Repository wrapping the Room inventory DAO. */
@Singleton
class InventoryRepository @Inject constructor(
    private val dao: InventoryDao
) {
    val allItems: Flow<List<InventoryItem>> = dao.getAll()
    val soldItems: Flow<List<InventoryItem>> = dao.getSold()
    val unsoldItems: Flow<List<InventoryItem>> = dao.getUnsold()

    suspend fun addItem(
        itemName: String,
        brand: String,
        category: String,
        condition: String,
        photoPath: String?,
        buyPrice: Double
    ): Long {
        return dao.insert(
            InventoryItem(
                itemName = itemName,
                brand = brand,
                category = category,
                condition = condition,
                photoPath = photoPath,
                buyPrice = buyPrice
            )
        )
    }

    suspend fun markAsSold(id: Long, soldPrice: Double) {
        val item = dao.getById(id) ?: return
        dao.update(
            item.copy(
                soldPrice = soldPrice,
                soldDate = System.currentTimeMillis()
            )
        )
    }

    suspend fun markAsUnsold(id: Long) {
        val item = dao.getById(id) ?: return
        dao.update(item.copy(soldPrice = null, soldDate = null))
    }

    suspend fun delete(item: InventoryItem) {
        dao.delete(item)
    }
}
