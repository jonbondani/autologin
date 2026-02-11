package com.autologin.app.domain.model

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val account: AccountInfo) : AuthState()
    data object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
