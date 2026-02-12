package com.autologin.app.domain.model

enum class SsoType {
    FULL,    // SSO automatico sin interaccion
    PARTIAL, // Pide confirmar usuario, no pide contraseña
}

data class DetectedApp(
    val packageName: String,
    val appName: String,
    val isInstalled: Boolean,
    val ssoType: SsoType = SsoType.FULL,
)
