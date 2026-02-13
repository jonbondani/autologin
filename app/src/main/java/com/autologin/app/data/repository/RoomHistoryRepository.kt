package com.autologin.app.data.repository

import com.autologin.app.data.local.AuthEvent
import com.autologin.app.data.local.AuthEventDao
import com.autologin.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomHistoryRepository @Inject constructor(
    private val dao: AuthEventDao,
) : HistoryRepository {

    override fun getAllEvents(): Flow<List<AuthEvent>> = dao.getAll()

    override fun getEventsByDateRange(startMillis: Long, endMillis: Long): Flow<List<AuthEvent>> =
        dao.getByDateRange(startMillis, endMillis)

    override suspend fun recordLogin(email: String, name: String) {
        dao.insert(
            AuthEvent(
                type = AuthEvent.TYPE_LOGIN,
                userEmail = email,
                userName = name,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun recordLogout(email: String, name: String) {
        dao.insert(
            AuthEvent(
                type = AuthEvent.TYPE_LOGOUT,
                userEmail = email,
                userName = name,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }
}
