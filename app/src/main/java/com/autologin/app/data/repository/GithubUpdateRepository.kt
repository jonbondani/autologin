package com.autologin.app.data.repository

import android.content.Context
import com.autologin.app.BuildConfig
import com.autologin.app.domain.model.AppUpdate
import com.autologin.app.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
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
}
