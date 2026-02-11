package com.autologin.app.data.repository

import android.content.pm.PackageManager
import com.autologin.app.domain.model.DetectedApp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDetector @Inject constructor(
    private val packageManager: PackageManager,
) {
    private val microsoftApps = mapOf(
        "com.microsoft.teams" to "Microsoft Teams",
        "com.microsoft.office.outlook" to "Outlook",
        "com.microsoft.skydrive" to "OneDrive",
        "com.microsoft.office.word" to "Word",
        "com.microsoft.office.excel" to "Excel",
        "com.microsoft.office.powerpoint" to "PowerPoint",
        "com.microsoft.sharepoint" to "SharePoint",
        "com.microsoft.todos" to "To Do",
        "com.azure.authenticator" to "Authenticator",
        "com.microsoft.windowsintune.companyportal" to "Company Portal",
    )

    fun getDetectedApps(): List<DetectedApp> {
        return microsoftApps.map { (pkg, name) ->
            DetectedApp(
                packageName = pkg,
                appName = name,
                isInstalled = isAppInstalled(pkg),
            )
        }.sortedByDescending { it.isInstalled }
    }

    fun isBrokerInstalled(): Boolean {
        return isAppInstalled("com.azure.authenticator") ||
            isAppInstalled("com.microsoft.windowsintune.companyportal")
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
