package com.autologin.app.data.repository

import com.autologin.app.data.local.AuthEvent
import com.autologin.app.data.local.AuthEventDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RoomHistoryRepositoryTest {

    private lateinit var fakeDao: FakeAuthEventDao
    private lateinit var repository: RoomHistoryRepository

    @Before
    fun setup() {
        fakeDao = FakeAuthEventDao()
        repository = RoomHistoryRepository(fakeDao)
    }

    @Test
    fun `recordLogin inserts LOGIN event`() = runTest {
        repository.recordLogin("test@company.com", "Test User")

        val events = fakeDao.getAll().first()
        assertEquals(1, events.size)
        assertEquals(AuthEvent.TYPE_LOGIN, events[0].type)
        assertEquals("test@company.com", events[0].userEmail)
        assertEquals("Test User", events[0].userName)
    }

    @Test
    fun `recordLogout inserts LOGOUT event`() = runTest {
        repository.recordLogout("test@company.com", "Test User")

        val events = fakeDao.getAll().first()
        assertEquals(1, events.size)
        assertEquals(AuthEvent.TYPE_LOGOUT, events[0].type)
    }

    @Test
    fun `getAllEvents returns events in insertion order`() = runTest {
        repository.recordLogin("user1@test.com", "User 1")
        repository.recordLogout("user1@test.com", "User 1")
        repository.recordLogin("user2@test.com", "User 2")

        val events = repository.getAllEvents().first()
        assertEquals(3, events.size)
        assertEquals(AuthEvent.TYPE_LOGIN, events[0].type)
        assertEquals(AuthEvent.TYPE_LOGOUT, events[1].type)
        assertEquals("user2@test.com", events[2].userEmail)
    }

    @Test
    fun `getEventsByDateRange filters correctly`() = runTest {
        fakeDao.insert(AuthEvent(type = AuthEvent.TYPE_LOGIN, userEmail = "a@t.com", userName = "A", timestamp = 1000))
        fakeDao.insert(AuthEvent(type = AuthEvent.TYPE_LOGIN, userEmail = "b@t.com", userName = "B", timestamp = 2000))
        fakeDao.insert(AuthEvent(type = AuthEvent.TYPE_LOGIN, userEmail = "c@t.com", userName = "C", timestamp = 3000))

        val events = repository.getEventsByDateRange(1500, 2500).first()
        assertEquals(1, events.size)
        assertEquals("b@t.com", events[0].userEmail)
    }
}

private class FakeAuthEventDao : AuthEventDao {
    private val events = mutableListOf<AuthEvent>()
    private val flow = MutableStateFlow<List<AuthEvent>>(emptyList())

    override fun getAll(): Flow<List<AuthEvent>> = flow

    override fun getByDateRange(startMillis: Long, endMillis: Long): Flow<List<AuthEvent>> {
        return MutableStateFlow(events.filter { it.timestamp in startMillis..endMillis })
    }

    override suspend fun insert(event: AuthEvent) {
        events.add(event)
        flow.value = events.toList()
    }
}
