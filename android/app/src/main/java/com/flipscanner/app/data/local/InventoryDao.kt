package com.flipscanner.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem): Long

    @Query("SELECT * FROM inventory_items ORDER BY createdAt DESC")
    fun getAll(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getById(id: Long): InventoryItem?

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Query("SELECT * FROM inventory_items WHERE soldPrice IS NOT NULL ORDER BY soldDate DESC")
    fun getSold(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE soldPrice IS NULL ORDER BY createdAt DESC")
    fun getUnsold(): Flow<List<InventoryItem>>
}
