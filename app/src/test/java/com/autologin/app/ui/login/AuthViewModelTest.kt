package com.autologin.app.ui.login

import android.app.Activity
import com.autologin.app.data.local.AuthEvent
import com.autologin.app.data.repository.AppDetector
import com.autologin.app.data.repository.MsalAuthRepository
import com.autologin.app.domain.model.AccountInfo
import com.autologin.app.domain.model.AuthState
import com.autologin.app.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: MsalAuthRepository
    private lateinit var appDetector: AppDetector
    private lateinit var historyRepository: FakeHistoryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock {
            on { authState }.thenReturn(MutableStateFlow(AuthState.Idle))
        }
        appDetector = mock {
            on { getDetectedApps() }.thenReturn(emptyList())
            on { isBrokerInstalled() }.thenReturn(true)
        }
        historyRepository = FakeHistoryRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn records login event on success`() = runTest {
        val account = AccountInfo("id1", "Test User", "test@company.com")
        whenever(authRepository.signIn(any())).thenReturn(Result.success(account))

        val viewModel = AuthViewModel(authRepository, appDetector, historyRepository)
        viewModel.signIn(mock<Activity>())

        assertEquals(1, historyRepository.events.size)
        assertEquals(AuthEvent.TYPE_LOGIN, historyRepository.events[0].type)
        assertEquals("test@company.com", historyRepository.events[0].userEmail)
    }

    @Test
    fun `signIn does not record event on failure`() = runTest {
        whenever(authRepository.signIn(any()))
            .thenReturn(Result.failure(Exception("cancelled")))

        val viewModel = AuthViewModel(authRepository, appDetector, historyRepository)
        viewModel.signIn(mock<Activity>())

        assertEquals(0, historyRepository.events.size)
    }

    @Test
    fun `signOut records logout event and kills apps`() = runTest {
        val account = AccountInfo("id1", "Test User", "test@company.com")
        whenever(authRepository.getAccount()).thenReturn(account)
        whenever(authRepository.signOut()).thenReturn(Result.success(Unit))

        val viewModel = AuthViewModel(authRepository, appDetector, historyRepository)
        viewModel.signOut()

        assertEquals(1, historyRepository.events.size)
        assertEquals(AuthEvent.TYPE_LOGOUT, historyRepository.events[0].type)
        verify(appDetector).killMicrosoftApps()
    }

    @Test
    fun `signOut without account does not record event`() = runTest {
        whenever(authRepository.getAccount()).thenReturn(null)
        whenever(authRepository.signOut()).thenReturn(Result.success(Unit))

        val viewModel = AuthViewModel(authRepository, appDetector, historyRepository)
        viewModel.signOut()

        assertEquals(0, historyRepository.events.size)
        verify(appDetector).killMicrosoftApps()
    }

    @Test
    fun `init detects apps and checks broker`() = runTest {
        AuthViewModel(authRepository, appDetector, historyRepository)

        verify(appDetector).getDetectedApps()
        verify(appDetector).isBrokerInstalled()
    }
}

private class FakeHistoryRepository : HistoryRepository {
    val events = mutableListOf<AuthEvent>()

    override fun getAllEvents(): Flow<List<AuthEvent>> = flowOf(events.toList())

    override fun getEventsByDateRange(startMillis: Long, endMillis: Long): Flow<List<AuthEvent>> =
        flowOf(events.filter { it.timestamp in startMillis..endMillis })

    override suspend fun recordLogin(email: String, name: String) {
        events.add(AuthEvent(type = AuthEvent.TYPE_LOGIN, userEmail = email, userName = name, timestamp = System.currentTimeMillis()))
    }

    override suspend fun recordLogout(email: String, name: String) {
        events.add(AuthEvent(type = AuthEvent.TYPE_LOGOUT, userEmail = email, userName = name, timestamp = System.currentTimeMillis()))
    }
}
