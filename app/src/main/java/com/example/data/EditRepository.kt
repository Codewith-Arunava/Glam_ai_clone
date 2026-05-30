package com.example.data

import kotlinx.coroutines.flow.Flow

class EditRepository(private val editDao: EditDao) {
    val allHistory: Flow<List<EditItem>> = editDao.getAllHistory()

    suspend fun saveItem(item: EditItem): Long {
        return editDao.insertItem(item)
    }

    suspend fun deleteItem(item: EditItem) {
        editDao.deleteItem(item)
    }

    suspend fun deleteById(id: Int) {
        editDao.deleteById(id)
    }

    suspend fun getItemById(id: Int): EditItem? {
        return editDao.getItemById(id)
    }
}
