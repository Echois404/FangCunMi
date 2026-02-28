package com.passmanager.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query("SELECT * FROM passwords ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE siteName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun search(query: String): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getById(id: Long): PasswordEntity?

    @Insert
    suspend fun insert(entity: PasswordEntity): Long

    @Update
    suspend fun update(entity: PasswordEntity)

    @Delete
    suspend fun delete(entity: PasswordEntity)

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deleteById(id: Long)
}
