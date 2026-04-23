package com.turbox.booster.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

object GameUtils {

    data class GameApp(
        val name: String,
        val packageName: String,
        val icon: android.graphics.drawable.Drawable?
    )

    // Detectar apps de juego instaladas
    fun getInstalledGames(context: Context): List<GameApp> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val games = mutableListOf<GameApp>()

        for (app in apps) {
            if (isGame(pm, app)) {
                try {
                    games.add(
                        GameApp(
                            name = pm.getApplicationLabel(app).toString(),
                            packageName = app.packageName,
                            icon = try { pm.getApplicationIcon(app.packageName) } catch (e: Exception) { null }
                        )
                    )
                } catch (e: Exception) { continue }
            }
        }
        return games.sortedBy { it.name }
    }

    private fun isGame(pm: PackageManager, appInfo: ApplicationInfo): Boolean {
        // Verificar categoría del juego (Android 7.1+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) return true
        }

        // Flag legacy de juego
        @Suppress("DEPRECATION")
        if (appInfo.flags and ApplicationInfo.FLAG_IS_GAME != 0) return true

        // Heurística por nombre del paquete
        val gamePackagePrefixes = listOf(
            "com.supercell", "com.king", "com.gameloft", "com.ea.",
            "com.activision", "com.squareenix", "com.bandainamco",
            "com.netease", "com.mihoyo", "com.tencent.ig",
            "com.dts.freefireth", "com.mobile.legends", "com.riotgames",
            "com.epicgames", "com.mojang", "com.nianticlabs",
            "com.scopely", "com.plarium", "com.kabam",
            "jp.konami", "com.ubisoft", "com.glu"
        )
        return gamePackagePrefixes.any { appInfo.packageName.startsWith(it) }
    }

    // Activar Game Mode de Android 12+
    fun activateGameMode(context: Context, packageName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val gameManager = context.getSystemService(android.app.GameManager::class.java)
                // GameManager requiere permiso especial, lo hacemos via Shizuku
                ShizukuHelper.setGameModePerformance(packageName)
                true
            } catch (e: Exception) {
                ShizukuHelper.setGameModePerformance(packageName)
                true
            }
        } else {
            ShizukuHelper.setGameModePerformance(packageName)
            true
        }
    }

    // Limpiar memoria antes de lanzar juego
    fun preLaunchBoost(context: Context) {
        // GC agresivo
        SystemUtils.triggerGarbageCollection()

        // Solicitar trim memory a todos los procesos visibles
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // Solo podemos trimear nuestra propia app
        am.clearApplicationUserData()
    }

    // Lanzar juego con optimizaciones
    fun launchGameOptimized(context: Context, packageName: String) {
        // Boost antes de lanzar
        SystemUtils.triggerGarbageCollection()

        // Si tenemos Shizuku, activar performance mode
        if (ShizukuHelper.isShizukuAvailable() && ShizukuHelper.hasShizukuPermission()) {
            ShizukuHelper.setGameModePerformance(packageName)
            ShizukuHelper.killAllBackgroundProcesses()
        }

        // Lanzar el juego
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        }
    }

    // Obtener estadísticas del juego actual
    fun getCurrentGameStats(context: Context, packageName: String): Map<String, String> {
        val stats = mutableMapOf<String, String>()
        val ram = SystemUtils.getRamInfo(context)
        val cpu = SystemUtils.getCpuUsage()
        val temp = SystemUtils.getCpuTemperature()

        stats["RAM libre"] = "${ram.availableMb} MB"
        stats["Uso CPU"] = "${cpu.toInt()}%"
        stats["Temperatura"] = "${temp.toInt()}°C"
        stats["Modo"] = if (ShizukuHelper.hasShizukuPermission()) "Performance" else "Normal"

        return stats
    }

    // Perfiles de rendimiento
    enum class PerformanceProfile {
        BATTERY_SAVER,  // Para juegos ligeros
        BALANCED,       // Default
        PERFORMANCE,    // Para juegos exigentes
        EXTREME         // Para juegos muy exigentes (baja duración de batería)
    }

    fun applyPerformanceProfile(context: Context, profile: PerformanceProfile) {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) return

        when (profile) {
            PerformanceProfile.BATTERY_SAVER -> {
                ShizukuHelper.setAnimations(0.5f)
                ShizukuHelper.executeCommand("settings put global low_power 1")
            }
            PerformanceProfile.BALANCED -> {
                ShizukuHelper.setAnimations(1.0f)
                ShizukuHelper.executeCommand("settings put global low_power 0")
            }
            PerformanceProfile.PERFORMANCE -> {
                ShizukuHelper.disableAnimations()
                ShizukuHelper.disableBatterySaver()
                ShizukuHelper.killAllBackgroundProcesses()
                ShizukuHelper.optimizeGpu()
            }
            PerformanceProfile.EXTREME -> {
                ShizukuHelper.disableAnimations()
                ShizukuHelper.disableBatterySaver()
                ShizukuHelper.killAllBackgroundProcesses()
                ShizukuHelper.optimizeGpu()
                ShizukuHelper.clearThermalThrottle()
                ShizukuHelper.setPerformanceMode()
                ShizukuHelper.setBackgroundProcessLimit(0)
            }
        }
    }
}
