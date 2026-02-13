package com.autologin.app.data.repository

import android.app.ActivityManager
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.autologin.app.domain.model.SsoType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppDetectorTest {

    private lateinit var packageManager: PackageManager
    private lateinit var activityManager: ActivityManager
    private lateinit var detector: AppDetector

    @Before
    fun setup() {
        packageManager = mock()
        activityManager = mock()
        detector = AppDetector(packageManager, activityManager)
    }

    @Test
    fun `isBrokerInstalled returns true when Authenticator is installed`() {
        whenever(packageManager.getPackageInfo(eq("com.azure.authenticator"), any<Int>()))
            .thenReturn(PackageInfo())
        whenever(packageManager.getPackageInfo(eq("com.microsoft.windowsintune.companyportal"), any<Int>()))
            .thenThrow(PackageManager.NameNotFoundException())

        assertTrue(detector.isBrokerInstalled())
    }

    @Test
    fun `isBrokerInstalled returns true when Company Portal is installed`() {
        whenever(packageManager.getPackageInfo(eq("com.azure.authenticator"), any<Int>()))
            .thenThrow(PackageManager.NameNotFoundException())
        whenever(packageManager.getPackageInfo(eq("com.microsoft.windowsintune.companyportal"), any<Int>()))
            .thenReturn(PackageInfo())

        assertTrue(detector.isBrokerInstalled())
    }

    @Test
    fun `isBrokerInstalled returns false when neither is installed`() {
        whenever(packageManager.getPackageInfo(any<String>(), any<Int>()))
            .thenThrow(PackageManager.NameNotFoundException())

        assertFalse(detector.isBrokerInstalled())
    }

    @Test
    fun `getDetectedApps returns correct SSO types`() {
        whenever(packageManager.getPackageInfo(any<String>(), any<Int>()))
            .thenThrow(PackageManager.NameNotFoundException())

        val apps = detector.getDetectedApps()

        val teams = apps.find { it.appName == "Microsoft Teams" }
        assertEquals(SsoType.FULL, teams?.ssoType)

        val word = apps.find { it.appName == "Word" }
        assertEquals(SsoType.PARTIAL, word?.ssoType)
    }

    @Test
    fun `getDetectedApps marks installed apps correctly`() {
        whenever(packageManager.getPackageInfo(eq("com.microsoft.teams"), any<Int>()))
            .thenReturn(PackageInfo())
        whenever(packageManager.getPackageInfo(any<String>(), any<Int>()))
            .thenThrow(PackageManager.NameNotFoundException())
        whenever(packageManager.getPackageInfo(eq("com.microsoft.teams"), any<Int>()))
            .thenReturn(PackageInfo())

        val apps = detector.getDetectedApps()
        val teams = apps.find { it.appName == "Microsoft Teams" }
        assertTrue(teams?.isInstalled == true)

        val word = apps.find { it.appName == "Word" }
        assertFalse(word?.isInstalled == true)
    }

    @Test
    fun `getDetectedApps sorts installed first then by SSO type`() {
        whenever(packageManager.getPackageInfo(eq("com.microsoft.office.word"), any<Int>()))
            .thenReturn(PackageInfo())
        whenever(packageManager.getPackageInfo(any<String>(), any<Int>()))
            .thenThrow(PackageManager.NameNotFoundException())
        whenever(packageManager.getPackageInfo(eq("com.microsoft.office.word"), any<Int>()))
            .thenReturn(PackageInfo())

        val apps = detector.getDetectedApps()
        val firstInstalled = apps.indexOfFirst { it.isInstalled }
        val lastInstalled = apps.indexOfLast { it.isInstalled }
        val firstNotInstalled = apps.indexOfFirst { !it.isInstalled }

        if (firstInstalled >= 0 && firstNotInstalled >= 0) {
            assertTrue(lastInstalled < firstNotInstalled)
        }
    }

    @Test
    fun `killMicrosoftApps calls killBackgroundProcesses for each app`() {
        detector.killMicrosoftApps()

        verify(activityManager).killBackgroundProcesses("com.microsoft.teams")
        verify(activityManager).killBackgroundProcesses("com.microsoft.office.officehubrow")
        verify(activityManager).killBackgroundProcesses("com.microsoft.emmx")
        verify(activityManager).killBackgroundProcesses("com.microsoft.office.onenote")
    }
}
