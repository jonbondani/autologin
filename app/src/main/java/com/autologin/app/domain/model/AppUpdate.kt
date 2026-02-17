package com.autologin.app.domain.model

data class AppUpdate(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
)
