package com.flipscanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One item in the user's flip inventory. */
@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val brand: String,
    val category: String,
    val condition: String,
    val photoPath: String?,
    val buyPrice: Double,
    /** Null while the item is still in stock. */
    val soldPrice: Double? = null,
    /** Epoch millis when the item sold; null while in stock. */
    val soldDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isSold: Boolean
        get() = soldPrice != null

    val profit: Double?
        get() = soldPrice?.minus(buyPrice)
}
