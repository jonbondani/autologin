package com.autologin.app.domain.repository

import com.autologin.app.data.local.AuthEvent
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllEvents(): Flow<List<AuthEvent>>
    fun getEventsByDateRange(startMillis: Long, endMillis: Long): Flow<List<AuthEvent>>
    suspend fun recordLogin(email: String, name: String)
    suspend fun recordLogout(email: String, name: String)
}
