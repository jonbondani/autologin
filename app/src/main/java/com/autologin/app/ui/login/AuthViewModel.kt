package com.autologin.app.ui.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologin.app.data.repository.AppDetector
import com.autologin.app.data.repository.MsalAuthRepository
import com.autologin.app.domain.model.AuthState
import com.autologin.app.domain.model.DetectedApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: MsalAuthRepository,
    private val appDetector: AppDetector,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState

    private val _detectedApps = MutableStateFlow<List<DetectedApp>>(emptyList())
    val detectedApps: StateFlow<List<DetectedApp>> = _detectedApps.asStateFlow()

    private val _brokerInstalled = MutableStateFlow(true)
    val brokerInstalled: StateFlow<Boolean> = _brokerInstalled.asStateFlow()

    val isSharedDevice: Boolean
        get() = authRepository.isSharedDevice

    init {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.initialize()
            _detectedApps.value = appDetector.getDetectedApps()
            _brokerInstalled.value = appDetector.isBrokerInstalled()
        }
    }

    fun signIn(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.signIn(activity)
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.signOut()
            appDetector.killMicrosoftApps()
        }
    }
}
