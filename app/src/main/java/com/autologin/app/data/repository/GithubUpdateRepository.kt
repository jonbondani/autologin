package com.autologin.app.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.autologin.app.BuildConfig
import com.autologin.app.domain.model.AppUpdate
import com.autologin.app.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject

class GithubUpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : UpdateRepository {

    companion object {
        private const val RELEASES_URL =
            "https://api.github.com/repos/jonbondani/autologin/releases/latest"
    }

    override suspend fun checkForUpdate(): AppUpdate? {
        val connection = URL(RELEASES_URL).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            if (connection.responseCode != 200) return null

            val json = JSONObject(connection.inputStream.bufferedReader().readText())
            val tagName = json.optString("tag_name", "") // e.g. "v25"
            val versionCode = tagName.removePrefix("v").toIntOrNull() ?: return null
            val versionName = json.optString("name", "")
            val releaseNotes = json.optString("body", "")

            if (versionCode <= BuildConfig.VERSION_CODE) return null

            val assets = json.optJSONArray("assets") ?: return null
            if (assets.length() == 0) return null
            val downloadUrl = assets.getJSONObject(0).optString("browser_download_url", "")
            if (downloadUrl.isEmpty()) return null

            return AppUpdate(
                versionName = versionName,
                versionCode = versionCode,
                downloadUrl = downloadUrl,
                releaseNotes = releaseNotes,
            )
        } catch (_: Exception) {
            return null
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun downloadApk(url: String, onProgress: (Int) -> Unit): File {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = File(updatesDir, "AutoLogin-update.apk")
        if (apkFile.exists()) apkFile.delete()

        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = true

            val totalBytes = connection.contentLength
            var downloadedBytes = 0

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            onProgress((downloadedBytes * 100 / totalBytes).coerceIn(0, 100))
                        }
                    }
                }
            }
            return apkFile
        } catch (e: Exception) {
            apkFile.delete()
            throw e
        } finally {
            connection.disconnect()
        }
    }

    override fun verifyApkSignature(apkFile: File): Boolean {
        return try {
            val downloadedSigs = getApkSignatures(apkFile.absolutePath) ?: return false
            val installedSigs = getInstalledSignatures() ?: return false

            if (downloadedSigs.isEmpty() || installedSigs.isEmpty()) return false

            // Compare SHA-256 of first signing certificate
            val downloadedHash = sha256(downloadedSigs.first().toByteArray())
            val installedHash = sha256(installedSigs.first().toByteArray())
            downloadedHash.contentEquals(installedHash)
        } catch (_: Exception) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun getApkSignatures(apkPath: String): Array<Signature>? {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
        val info: PackageInfo = context.packageManager
            .getPackageArchiveInfo(apkPath, flags) ?: return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners
        } else {
            info.signatures
        }
    }

    @Suppress("DEPRECATION")
    private fun getInstalledSignatures(): Array<Signature>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )
            info.signingInfo?.apkContentsSigners
        } else {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES,
            )
            info.signatures
        }
    }

    private fun sha256(bytes: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(bytes)
    }
}
