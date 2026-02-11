package com.autologin.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthEventDao {

    @Query("SELECT * FROM auth_events ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AuthEvent>>

    @Query("SELECT * FROM auth_events WHERE timestamp BETWEEN :startMillis AND :endMillis ORDER BY timestamp DESC")
    fun getByDateRange(startMillis: Long, endMillis: Long): Flow<List<AuthEvent>>

    @Insert
    suspend fun insert(event: AuthEvent)
}
