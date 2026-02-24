package com.autologin.app.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologin.app.BuildConfig
import com.autologin.app.data.repository.AppDetector
import com.autologin.app.data.repository.MsalAuthRepository
import com.autologin.app.domain.model.AppUpdate
import com.autologin.app.domain.model.AuthState
import com.autologin.app.domain.model.DetectedApp
import com.autologin.app.domain.repository.HistoryRepository
import com.autologin.app.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class UpdateState {
    data object NoUpdate : UpdateState()
    data class Available(val update: AppUpdate) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val file: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: MsalAuthRepository,
    private val appDetector: AppDetector,
    private val historyRepository: HistoryRepository,
    private val updateRepository: UpdateRepository,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState

    private val _detectedApps = MutableStateFlow<List<DetectedApp>>(emptyList())
    val detectedApps: StateFlow<List<DetectedApp>> = _detectedApps.asStateFlow()

    private val _brokerInstalled = MutableStateFlow(true)
    val brokerInstalled: StateFlow<Boolean> = _brokerInstalled.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.NoUpdate)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    val isSharedDevice: Boolean
        get() = authRepository.isSharedDevice

    init {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.initialize()
            _detectedApps.value = appDetector.getDetectedApps()
            _brokerInstalled.value = appDetector.isBrokerInstalled()
        }
        viewModelScope.launch(Dispatchers.IO) {
            checkForUpdate()
        }
    }

    fun signIn(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.signIn(activity)
            result.getOrNull()?.let { account ->
                historyRepository.recordLogin(account.email, account.name)
            }
        }
    }

    fun getLaunchIntent(packageName: String): Intent? = appDetector.getLaunchIntent(packageName)

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            logDebug("ViewModel.signOut() started")
            logDebug("  isSharedDevice = ${authRepository.isSharedDevice}")
            val account = authRepository.getAccount()
            logDebug("  current account = ${account?.email ?: "null"}")

            val result = authRepository.signOut()
            logDebug("  signOut result = ${if (result.isSuccess) "SUCCESS" else "FAILURE: ${result.exceptionOrNull()?.message}"}")

            account?.let { historyRepository.recordLogout(it.email, it.name) }

            logDebug("  killing Microsoft apps...")
            appDetector.killMicrosoftApps()
            logDebug("  signOut flow complete")

            // Post-signout: check if account is truly gone
            val postAccount = authRepository.getAccount()
            logDebug("  post-signOut account = ${postAccount?.email ?: "null (OK)"}")
        }
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("AutoLogin", message)
        }
    }

    private suspend fun checkForUpdate() {
        try {
            val update = updateRepository.checkForUpdate()
            _updateState.value = if (update != null) UpdateState.Available(update) else UpdateState.NoUpdate
        } catch (_: Exception) {
            // Silent fail — don't bother user if update check fails
        }
    }

    fun downloadUpdate() {
        val current = _updateState.value
        if (current !is UpdateState.Available) return
        val url = current.update.downloadUrl

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _updateState.value = UpdateState.Downloading(0)
                val file = updateRepository.downloadApk(url) { progress ->
                    _updateState.value = UpdateState.Downloading(progress)
                }
                if (!updateRepository.verifyApkSignature(file)) {
                    file.delete()
                    _updateState.value = UpdateState.Error("La firma del APK no coincide. Actualizacion rechazada.")
                    return@launch
                }
                _updateState.value = UpdateState.ReadyToInstall(file)
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Error de descarga")
            }
        }
    }

    fun installUpdate(context: Context) {
        val current = _updateState.value
        if (current !is UpdateState.ReadyToInstall) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            current.file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun dismissUpdateError() {
        _updateState.value = UpdateState.NoUpdate
    }
}
