package com.turbox.booster.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

object AppManagerUtils {

    data class AppInfo(
        val name: String,
        val packageName: String,
        val versionName: String,
        val sizeBytes: Long,
        val isSystemApp: Boolean,
        val isEnabled: Boolean,
        val targetSdk: Int,
        val icon: android.graphics.drawable.Drawable?
    )

    // Obtener todas las apps del usuario (no sistema)
    fun getUserApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .mapNotNull { app ->
                try {
                    val pkgInfo = pm.getPackageInfo(app.packageName, 0)
                    AppInfo(
                        name = pm.getApplicationLabel(app).toString(),
                        packageName = app.packageName,
                        versionName = pkgInfo.versionName ?: "?",
                        sizeBytes = try { pm.getApplicationInfo(app.packageName, 0).let { 0L } } catch (e: Exception) { 0L },
                        isSystemApp = false,
                        isEnabled = app.enabled,
                        targetSdk = app.targetSdkVersion,
                        icon = try { pm.getApplicationIcon(app.packageName) } catch (e: Exception) { null }
                    )
                } catch (e: Exception) { null }
            }
            .sortedBy { it.name }
    }

    // Obtener apps del sistema que se pueden deshabilitar de forma segura
    fun getDisablableSystemApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val bloatware = getSafeBloatwarePackages()

        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                app.flags and ApplicationInfo.FLAG_SYSTEM != 0 &&
                bloatware.any { app.packageName.startsWith(it) }
            }
            .mapNotNull { app ->
                try {
                    AppInfo(
                        name = pm.getApplicationLabel(app).toString(),
                        packageName = app.packageName,
                        versionName = "",
                        sizeBytes = 0L,
                        isSystemApp = true,
                        isEnabled = app.enabled,
                        targetSdk = app.targetSdkVersion,
                        icon = try { pm.getApplicationIcon(app.packageName) } catch (e: Exception) { null }
                    )
                } catch (e: Exception) { null }
            }
            .sortedBy { it.name }
    }

    // Apps bloatware conocidas seguras para deshabilitar
    fun getSafeBloatwarePackages(): List<String> {
        return listOf(
            // Google bloatware
            "com.google.android.videos",
            "com.google.android.music",
            "com.google.android.apps.tachyon",
            "com.google.android.apps.stickers",
            "com.google.android.feedback",
            "com.google.android.onetimeinitializer",
            "com.google.android.marvin.talkback",
            "com.google.android.accessibility",
            // Samsung bloatware
            "com.samsung.android.forest",
            "com.samsung.android.game.gametools",
            "com.samsung.android.kidsinstaller",
            "com.samsung.android.bixby",
            "com.samsung.android.app.routines",
            "com.sec.android.app.kidshome",
            "com.samsung.android.app.social",
            "com.samsung.android.samsungpassautofill",
            // Xiaomi bloatware
            "com.miui.bugreport",
            "com.miui.cleanmaster",
            "com.miui.yellowpage",
            "com.xiaomi.mipicks",
            // OPPO/OnePlus bloatware
            "com.coloros.gamespace",
            "com.nearme.gamecenter",
            // Misc
            "com.facebook.appmanager",
            "com.facebook.services",
            "com.facebook.system"
        )
    }

    // Forzar cierre de una app
    fun forceStopApp(packageName: String) {
        if (ShizukuHelper.isShizukuAvailable() && ShizukuHelper.hasShizukuPermission()) {
            ShizukuHelper.forceStopApp(packageName)
        }
    }

    // Deshabilitar una app (vía Shizuku)
    fun disableApp(packageName: String): Boolean {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) return false
        return try {
            ShizukuHelper.disablePackage(packageName)
            true
        } catch (e: Exception) { false }
    }

    // Habilitar una app (vía Shizuku)
    fun enableApp(packageName: String): Boolean {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) return false
        return try {
            ShizukuHelper.enablePackage(packageName)
            true
        } catch (e: Exception) { false }
    }

    // Calcular cuántas apps están corriendo en background
    fun getBackgroundAppsCount(context: Context): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return am.runningAppProcesses?.size ?: 0
    }

    // Limpiar caché de una app específica
    fun clearAppCache(context: Context, packageName: String) {
        if (ShizukuHelper.isShizukuAvailable() && ShizukuHelper.hasShizukuPermission()) {
            ShizukuHelper.executeCommand("pm clear --cache-only $packageName")
        }
    }
}
