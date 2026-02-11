package com.autologin.app.data.repository

import android.app.Activity
import android.content.Context
import com.autologin.app.R
import com.autologin.app.domain.model.AccountInfo
import com.autologin.app.domain.model.AuthState
import com.autologin.app.domain.repository.AuthRepository
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
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

    override val isSharedDevice: Boolean
        get() = msalApp?.isSharedDevice == true

    suspend fun initialize() {
        try {
            msalApp = suspendCancellableCoroutine { continuation ->
                PublicClientApplication.createSingleAccountPublicClientApplication(
                    context,
                    R.raw.auth_config,
                    object : com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                        override fun onCreated(application: ISingleAccountPublicClientApplication) {
                            continuation.resume(application)
                        }

                        override fun onError(exception: MsalException) {
                            continuation.resume(null)
                        }
                    },
                )
            }
            loadExistingAccount()
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Error al inicializar MSAL: ${e.message}")
        }
    }

    private fun loadExistingAccount() {
        val app = msalApp ?: return
        try {
            val accountResult = app.currentAccount
            val account = accountResult?.currentAccount
            if (account != null) {
                _authState.value = AuthState.Authenticated(account.toAccountInfo())
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    override suspend fun signIn(activity: Activity): Result<AccountInfo> {
        val app = msalApp ?: return Result.failure(Exception("MSAL no inicializado"))
        _authState.value = AuthState.Loading

        return suspendCancellableCoroutine { continuation ->
            val params = AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .withScopes(listOf("User.Read"))
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(result: IAuthenticationResult) {
                        val info = result.account.toAccountInfo()
                        _authState.value = AuthState.Authenticated(info)
                        continuation.resume(Result.success(info))
                    }

                    override fun onError(exception: MsalException) {
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

            app.acquireToken(params)
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
