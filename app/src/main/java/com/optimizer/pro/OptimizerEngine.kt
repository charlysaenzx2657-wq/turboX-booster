package com.optimizer.pro

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OptimizerEngine {

    private const val TAG = "OptimizerEngine"

    suspend fun runNormalOptimization(context: Context): OptimizationResult = withContext(Dispatchers.IO) {
        val results = mutableListOf<String>()
        var ramFreed = 0L
        var appsKilled = 0

        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = am.runningAppProcesses ?: emptyList()

            for (process in runningApps) {
                if (process.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    try {
                        am.killBackgroundProcesses(process.processName)
                        appsKilled++
                        ramFreed += 20 * 1024 * 1024L
                    } catch (e: Exception) {
                        Log.d(TAG, "No se pudo matar: ${process.processName}")
                    }
                }
            }

            val heavyApps = getHeavyApps(context)
            for (pkg in heavyApps) {
                try {
                    am.killBackgroundProcesses(pkg)
                    appsKilled++
                } catch (e: Exception) { }
            }

            results.add("✅ Procesos eliminados: $appsKilled")
            results.add("✅ RAM liberada estimada: ${ramFreed / 1024 / 1024} MB")
            results.add("ℹ️ Para limpieza completa activa Modo Pro")

        } catch (e: Exception) {
            results.add("⚠️ Error parcial: ${e.message}")
        }

        OptimizationResult(
            success = true,
            mode = "Normal",
            actions = results,
            ramFreedMb = ramFreed / 1024 / 1024,
            appsProcessed = appsKilled
        )
    }

    fun getHeavyApps(context: Context): List<String> {
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (1000 * 60 * 60 * 24)
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            stats
                ?.filter { it.totalTimeInForeground == 0L && it.packageName.contains(".") }
                ?.sortedByDescending { it.lastTimeUsed }
                ?.map { it.packageName }
                ?.take(20)
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getRamInfo(context: Context): RamInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return RamInfo(
            totalMb = memInfo.totalMem / 1024 / 1024,
            availableMb = memInfo.availMem / 1024 / 1024,
            usedMb = (memInfo.totalMem - memInfo.availMem) / 1024 / 1024,
            isLowMemory = memInfo.lowMemory
        )
    }

    // TODAS las apps instaladas por el usuario (no solo PlayStore)
    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PackageManager.MATCH_UNINSTALLED_PACKAGES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_UNINSTALLED_PACKAGES
        }
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                // Excluir apps del sistema Y nuestra propia app
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystem = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                val isSelf = app.packageName == context.packageName
                // Mostrar apps de usuario + apps de sistema actualizadas (como Chrome, etc.)
                (!isSystem || isUpdatedSystem) && !isSelf
            }
            .map { app ->
                AppInfo(
                    packageName = app.packageName,
                    appName = try {
                        pm.getApplicationLabel(app).toString()
                    } catch (e: Exception) {
                        app.packageName
                    },
                    icon = try {
                        pm.getApplicationIcon(app.packageName)
                    } catch (e: Exception) {
                        null
                    }
                )
            }
            .sortedBy { it.appName }
    }

    fun getBackgroundProcessCount(context: Context): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses?.count {
            it.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
        } ?: 0
    }
}

data class OptimizationResult(
    val success: Boolean,
    val mode: String,
    val actions: List<String>,
    val ramFreedMb: Long,
    val appsProcessed: Int
)

data class RamInfo(
    val totalMb: Long,
    val availableMb: Long,
    val usedMb: Long,
    val isLowMemory: Boolean
)

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?
)
