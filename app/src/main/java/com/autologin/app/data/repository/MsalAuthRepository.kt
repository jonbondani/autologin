package com.autologin.app.data.repository

import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
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
            val result = suspendCancellableCoroutine { continuation ->
                PublicClientApplication.createSingleAccountPublicClientApplication(
                    context,
                    R.raw.auth_config,
                    object : com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                        override fun onCreated(application: ISingleAccountPublicClientApplication) {
                            continuation.resume(Result.success(application))
                        }

                        override fun onError(exception: MsalException) {
                            Log.e("AutoLogin", "MSAL init error: ${exception.message}", exception)
                            continuation.resume(Result.failure(exception))
                        }
                    },
                )
            }

            result.onSuccess { app ->
                msalApp = app
                Log.d("AutoLogin", "MSAL OK. Shared device: ${app.isSharedDevice}")
                loadExistingAccount()
            }.onFailure { e ->
                initError = e.message
                _authState.value = AuthState.Error("MSAL error: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("AutoLogin", "MSAL init exception: ${e.message}", e)
            initError = e.message
            _authState.value = AuthState.Error("MSAL exception: ${e.message}")
        }
    }

    private fun loadExistingAccount() {
        val app = msalApp ?: return
        try {
            val accountResult = app.currentAccount
            val account = accountResult?.currentAccount
            Log.d("AutoLogin", "loadExistingAccount: account=${account?.username}, priorAccount=${accountResult?.priorAccount?.username}")
            if (account != null) {
                _authState.value = AuthState.Authenticated(account.toAccountInfo())
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            Log.e("AutoLogin", "loadExistingAccount error: ${e.message}", e)
            _authState.value = AuthState.Unauthenticated
        }
    }

    override suspend fun signIn(activity: Activity): Result<AccountInfo> {
        val app = msalApp ?: run {
            _authState.value = AuthState.Error("MSAL no inicializado. Error original: ${initError ?: "desconocido"}")
            return Result.failure(Exception("MSAL no inicializado"))
        }

        // Si ya hay cuenta, mostrarla directamente
        try {
            val existing = app.currentAccount?.currentAccount
            if (existing != null) {
                val info = existing.toAccountInfo()
                Log.d("AutoLogin", "Cuenta ya existente: ${info.email}")
                _authState.value = AuthState.Authenticated(info)
                return Result.success(info)
            }
        } catch (e: Exception) {
            Log.e("AutoLogin", "Error checking existing account: ${e.message}")
        }

        _authState.value = AuthState.Loading

        return suspendCancellableCoroutine { continuation ->
            val params = SignInParameters.builder()
                .withActivity(activity)
                .withScopes(listOf("User.Read"))
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(result: IAuthenticationResult) {
                        val info = result.account.toAccountInfo()
                        Log.d("AutoLogin", "Global sign-in OK: ${info.email}, shared: ${app.isSharedDevice}")
                        _authState.value = AuthState.Authenticated(info)
                        continuation.resume(Result.success(info))
                    }

                    override fun onError(exception: MsalException) {
                        Log.e("AutoLogin", "Sign-in error: ${exception.message}", exception)
                        // Si falla porque ya hay cuenta, recuperarla
                        try {
                            val account = app.currentAccount?.currentAccount
                            if (account != null) {
                                val info = account.toAccountInfo()
                                _authState.value = AuthState.Authenticated(info)
                                continuation.resume(Result.success(info))
                                return
                            }
                        } catch (_: Exception) {}
                        val msg = exception.message ?: "Error de autenticacion"
                        _authState.value = AuthState.Error(msg)
                        continuation.resume(Result.failure(exception))
                    }

                    override fun onCancel() {
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
        _authState.value = AuthState.Loading

        return suspendCancellableCoroutine { continuation ->
            app.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    _authState.value = AuthState.Unauthenticated
                    continuation.resume(Result.success(Unit))
                }

                override fun onError(exception: MsalException) {
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
}
