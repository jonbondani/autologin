package com.autologin.app.domain.repository

import com.autologin.app.domain.model.AppUpdate
import java.io.File

interface UpdateRepository {
    suspend fun checkForUpdate(): AppUpdate?
    suspend fun downloadApk(url: String, onProgress: (Int) -> Unit): File
    fun verifyApkSignature(apkFile: File): Boolean
}
