package com.autologin.app.data.repository

import android.app.Activity
import android.content.Context
import com.autologin.app.BuildConfig
import com.autologin.app.R
import com.autologin.app.domain.model.AccountInfo
import com.autologin.app.domain.model.AuthState
import com.autologin.app.domain.repository.AuthRepository
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class MsalAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var msalApp: ISingleAccountPublicClientApplication? = null
    private var initError: String? = null

    override val isSharedDevice: Boolean
        get() = msalApp?.isSharedDevice == true

    suspend fun initialize() {
        try {
            val app = suspendCancellableCoroutine { continuation ->
                PublicClientApplication.createSingleAccountPublicClientApplication(
                    context,
                    R.raw.auth_config,
                    object : com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                        override fun onCreated(application: ISingleAccountPublicClientApplication) {
                            continuation.resume(application)
                        }

                        override fun onError(exception: MsalException) {
                            logDebug("MSAL init error: ${exception.message}")
                            continuation.resume(null)
                        }
                    },
                )
            }

            if (app != null) {
                msalApp = app
                logDebug("MSAL init OK")
                logDebug("  isSharedDevice = ${app.isSharedDevice}")
                logDebug("  app class = ${app.javaClass.simpleName}")
                if (!app.isSharedDevice) {
                    logDebug("  WARNING: Device NOT in Shared Device Mode. Sign-out will be local only.")
                    logDebug("  To fix: Register device as shared in Microsoft Authenticator / Intune.")
                }
                loadExistingAccount()
            } else {
                _authState.value = AuthState.Error("No se pudo inicializar MSAL")
            }
        } catch (e: Exception) {
            logDebug("MSAL init exception: ${e.message}")
            initError = e.message
            _authState.value = AuthState.Error("MSAL exception: ${e.message}")
        }
    }

    private suspend fun loadExistingAccount() {
        val app = msalApp ?: return
        try {
            val account = withContext(Dispatchers.IO) {
                app.currentAccount?.currentAccount
            }
            logDebug("loadExistingAccount: hasAccount=${account != null}")
            if (account != null) {
                _authState.value = AuthState.Authenticated(account.toAccountInfo())
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            logDebug("loadExistingAccount error: ${e.message}")
            _authState.value = AuthState.Unauthenticated
        }
    }

    override suspend fun signIn(activity: Activity): Result<AccountInfo> {
        val app = msalApp ?: run {
            _authState.value = AuthState.Error("MSAL no inicializado. Error original: ${initError ?: "desconocido"}")
            return Result.failure(Exception("MSAL no inicializado"))
        }

        logDebug("signIn() called. isSharedDevice=${app.isSharedDevice}")

        // Si ya hay cuenta, mostrarla directamente
        try {
            val existing = withContext(Dispatchers.IO) { app.currentAccount?.currentAccount }
            if (existing != null) {
                val info = existing.toAccountInfo()
                logDebug("signIn: Existing account found, reusing session")
                _authState.value = AuthState.Authenticated(info)
                return Result.success(info)
            }
        } catch (e: Exception) {
            logDebug("signIn: Error checking existing account: ${e.message}")
        }

        _authState.value = AuthState.Loading

        return suspendCancellableCoroutine { continuation ->
            val params = SignInParameters.builder()
                .withActivity(activity)
                .withScopes(listOf("User.Read"))
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(result: IAuthenticationResult) {
                        val info = result.account.toAccountInfo()
                        logDebug("signIn: SUCCESS. shared=${app.isSharedDevice}, tenantId=${result.tenantId}")
                        _authState.value = AuthState.Authenticated(info)
                        continuation.resume(Result.success(info))
                    }

                    override fun onError(exception: MsalException) {
                        logDebug("signIn: ERROR: ${exception.errorCode} - ${exception.message}")
                        val msg = exception.message ?: "Error de autenticacion"
                        _authState.value = AuthState.Error(msg)
                        continuation.resume(Result.failure(exception))
                    }

                    override fun onCancel() {
                        logDebug("signIn: CANCELLED by user")
                        _authState.value = AuthState.Unauthenticated
                        continuation.resume(Result.failure(Exception("Login cancelado por el usuario")))
                    }
                })
                .build()

            app.signIn(params)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        val app = msalApp ?: return Result.failure(Exception("MSAL no inicializado"))

        // Pre-signout diagnostics
        logDebug("signOut() called")
        logDebug("  isSharedDevice = ${app.isSharedDevice}")
        try {
            val preAccount = withContext(Dispatchers.IO) { app.currentAccount?.currentAccount }
            logDebug("  pre-signOut account = ${if (preAccount != null) "present (${preAccount.username})" else "null"}")
        } catch (e: Exception) {
            logDebug("  pre-signOut account check failed: ${e.message}")
        }

        if (!app.isSharedDevice) {
            logDebug("  WARNING: NOT shared device mode. signOut() will only clear local MSAL cache.")
            logDebug("  Other apps (Teams, Edge, etc.) will keep their sessions via broker PRT.")
            logDebug("  To fix: Register device as shared in Microsoft Authenticator.")
        }

        _authState.value = AuthState.Loading

        return suspendCancellableCoroutine { continuation ->
            app.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    logDebug("signOut: SUCCESS callback received")
                    try {
                        val postAccount = app.currentAccount?.currentAccount
                        logDebug("  post-signOut account = ${if (postAccount != null) "STILL PRESENT — local cache NOT cleared" else "null (local cache cleared OK)"}")
                    } catch (e: Exception) {
                        logDebug("  post-signOut account check failed: ${e.message}")
                    }
                    _authState.value = AuthState.Unauthenticated
                    continuation.resume(Result.success(Unit))
                }

                override fun onError(exception: MsalException) {
                    logDebug("signOut: ERROR: ${exception.errorCode} - ${exception.message}")
                    val msg = exception.message ?: "Error al cerrar sesion"
                    _authState.value = AuthState.Error(msg)
                    continuation.resume(Result.failure(exception))
                }
            })
        }
    }

    override fun getAccount(): AccountInfo? {
        return try {
            msalApp?.currentAccount?.currentAccount?.toAccountInfo()
        } catch (e: Exception) {
            null
        }
    }

    private fun IAccount.toAccountInfo(): AccountInfo {
        return AccountInfo(
            id = id ?: "",
            name = claims?.get("name")?.toString() ?: username ?: "",
            email = username ?: "",
        )
    }

    /** Log only in debug builds to avoid PII leaks in production logcat. */
    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("AutoLogin", message)
        }
    }
}
