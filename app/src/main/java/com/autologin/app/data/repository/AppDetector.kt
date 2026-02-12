package com.autologin.app.data.repository

import android.app.ActivityManager
import android.content.pm.PackageManager
import android.util.Log
import com.autologin.app.domain.model.DetectedApp
import com.autologin.app.domain.model.SsoType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDetector @Inject constructor(
    private val packageManager: PackageManager,
    private val activityManager: ActivityManager,
) {
    // Apps con SSO completo (shared device mode aware)
    private val ssoFullApps = mapOf(
        "com.microsoft.office.officehub" to "Microsoft 365 Copilot",
        "com.microsoft.teams" to "Microsoft Teams",
        "com.microsoft.emmx" to "Microsoft Edge",
    )

    // Apps con SSO parcial (broker PRT, pide confirmar usuario)
    private val ssoPartialApps = mapOf(
        "com.microsoft.skydrive" to "OneDrive",
        "com.microsoft.office.word" to "Word",
        "com.microsoft.office.excel" to "Excel",
        "com.microsoft.office.powerpoint" to "PowerPoint",
        "com.microsoft.sharepoint" to "SharePoint",
        "com.microsoft.todos" to "To Do",
    )

    // Apps a limpiar en logout (excluye Authenticator y Company Portal)
    private val appsToClean = listOf(
        "com.microsoft.office.officehub",
        "com.microsoft.teams",
        "com.microsoft.emmx",
        "com.microsoft.skydrive",
        "com.microsoft.office.word",
        "com.microsoft.office.excel",
        "com.microsoft.office.powerpoint",
        "com.microsoft.sharepoint",
        "com.microsoft.todos",
        "com.microsoft.onenote",
    )

    fun getDetectedApps(): List<DetectedApp> {
        val full = ssoFullApps.map { (pkg, name) ->
            DetectedApp(pkg, name, isAppInstalled(pkg), SsoType.FULL)
        }
        val partial = ssoPartialApps.map { (pkg, name) ->
            DetectedApp(pkg, name, isAppInstalled(pkg), SsoType.PARTIAL)
        }
        return (full + partial).sortedWith(
            compareByDescending<DetectedApp> { it.isInstalled }
                .thenBy { it.ssoType.ordinal },
        )
    }

    fun isBrokerInstalled(): Boolean {
        return isAppInstalled("com.azure.authenticator") ||
            isAppInstalled("com.microsoft.windowsintune.companyportal")
    }

    fun killMicrosoftApps() {
        appsToClean.forEach { pkg ->
            try {
                activityManager.killBackgroundProcesses(pkg)
                Log.d("AutoLogin", "Killed background process: $pkg")
            } catch (e: Exception) {
                Log.w("AutoLogin", "Could not kill $pkg: ${e.message}")
            }
        }
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
