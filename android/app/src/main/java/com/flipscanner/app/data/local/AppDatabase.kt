package com.flipscanner.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [InventoryItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
}
