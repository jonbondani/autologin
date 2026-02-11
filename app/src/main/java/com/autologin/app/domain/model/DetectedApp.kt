package com.autologin.app.domain.model

data class DetectedApp(
    val packageName: String,
    val appName: String,
    val isInstalled: Boolean,
)
