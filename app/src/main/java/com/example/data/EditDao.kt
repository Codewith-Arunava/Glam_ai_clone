package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EditDao {
    @Query("SELECT * FROM edit_items ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<EditItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: EditItem): Long

    @Delete
    suspend fun deleteItem(item: EditItem)

    @Query("DELETE FROM edit_items WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM edit_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): EditItem?
}
