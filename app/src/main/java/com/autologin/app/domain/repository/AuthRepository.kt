package com.autologin.app.domain.repository

import android.app.Activity
import com.autologin.app.domain.model.AccountInfo
import com.autologin.app.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    val isSharedDevice: Boolean
    suspend fun signIn(activity: Activity): Result<AccountInfo>
    suspend fun signOut(): Result<Unit>
    fun getAccount(): AccountInfo?
}
