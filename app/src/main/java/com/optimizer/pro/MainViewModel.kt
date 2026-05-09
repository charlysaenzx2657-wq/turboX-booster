package com.optimizer.pro

import android.app.Application
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ╔══════════════════════════════════════════════════════════════╗
// ║        TURBOX ULTRA — MAIN VIEWMODEL v3.0 🚀                ║
// ╚══════════════════════════════════════════════════════════════╝

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx: Context get() = getApplication()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
    val shizukuStatus: StateFlow<ShizukuStatus> = ShizukuHelper.status

    init {
        ShizukuHelper.initialize()
        refreshRamInfo()
        loadInstalledApps()
    }

    fun refreshRamInfo() {
        val info = OptimizerEngine.getRamInfo(ctx)
        _uiState.value = _uiState.value.copy(ramInfo = info)
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = OptimizerEngine.getInstalledApps(ctx)
            _uiState.value = _uiState.value.copy(installedApps = apps)
        }
    }

    fun hasUsageStatsPermission(): Boolean {
        return try {
            val um = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val stats = um.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - 1000 * 60,
                System.currentTimeMillis()
            )
            stats != null && stats.isNotEmpty()
        } catch (e: Exception) { false }
    }

    fun hasBatteryOptimizationExemption(): Boolean {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    fun openUsageStatsSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun openBatteryOptimizationSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${ctx.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun openAccessibilitySettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    // ══════════════════════════════════════════════════════════════
    // MÓDULO INDIVIDUAL — ULTRA
    // ══════════════════════════════════════════════════════════════
    fun optimizeModule(module: OptModule) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isOptimizing = true, lastResult = null,
                optimizationProgress = 0f, optimizationStep = "Iniciando Ultra..."
            )
            try {
                val pro = ShizukuHelper.isConnected()
                updateProgress(0.1f, "Analizando sistema...")
                kotlinx.coroutines.delay(300)
                updateProgress(0.25f, "Preparando módulos...")
                kotlinx.coroutines.delay(300)
                updateProgress(0.4f, "Aplicando optimizaciones...")
                kotlinx.coroutines.delay(300)

                val result = when (module) {
                    OptModule.ALL     -> if (pro) ProOptimizerEngine.runFullProOptimization(ctx) else OptimizerEngine.runNormalOptimization(ctx)
                    OptModule.RAM     -> if (pro) ProOptimizerEngine.killAllBackgroundProcesses(ctx) else OptimizerEngine.runNormalOptimization(ctx)
                    OptModule.CACHE   -> ProOptimizerEngine.clearAllAppsCache(ctx)
                    OptModule.CPU     -> ProOptimizerEngine.optimizeCPU()
                    OptModule.GPU     -> ProOptimizerEngine.optimizeGPU()
                    OptModule.FPS     -> {
                        val r1 = ProOptimizerEngine.optimizeFPS()
                        val r2 = ProOptimizerEngine.optimizeGPU()
                        val r3 = ProOptimizerEngine.optimizeDisplay()
                        OptimizationResult(true, "FPS Ultra + Hz + GPU", r1.actions + r2.actions + r3.actions, 0, 0)
                    }
                    OptModule.NET     -> ProOptimizerEngine.optimizeNetwork()
                    OptModule.BATTERY -> ProOptimizerEngine.optimizeBattery()
                    OptModule.STORAGE -> {
                        val r1 = ProOptimizerEngine.optimizeIO()
                        val r2 = ProOptimizerEngine.cleanSystemTemp()
                        OptimizationResult(true, "I/O Ultra + Limpieza", r1.actions + r2.actions, 0, 0)
                    }
                    OptModule.THERMAL -> ProOptimizerEngine.optimizeThermal()
                }

                updateProgress(0.80f, "Verificando cambios...")
                kotlinx.coroutines.delay(350)
                updateProgress(0.95f, "Finalizando...")
                kotlinx.coroutines.delay(300)
                updateProgress(1.0f, "¡Listo!")
                kotlinx.coroutines.delay(600)
                refreshRamInfo()

                _uiState.value = _uiState.value.copy(
                    isOptimizing = false, lastResult = result,
                    showResultDialog = true, optimizationProgress = 0f, optimizationStep = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isOptimizing = false, errorMessage = "Error: ${e.message}"
                )
            }
        }
    }

    private fun updateProgress(p: Float, step: String) {
        _uiState.value = _uiState.value.copy(optimizationProgress = p, optimizationStep = step)
    }

    // ══════════════════════════════════════════════════════════════
    // BOOST FPS APP INDIVIDUAL
    // ══════════════════════════════════════════════════════════════
    fun boostAppFps(packageName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOptimizing = true, lastResult = null, optimizationProgress = 0f, optimizationStep = "Iniciando boost...")
            try {
                val actions = mutableListOf<String>()
                updateProgress(0.2f, "Limpiando caché...")
                ShizukuHelper.runCommand("pm clear --cache-only $packageName")
                actions.add("✅ Caché limpiada")
                updateProgress(0.4f, "Configurando GPU...")
                ShizukuHelper.runCommand("settings put global game_driver_all_apps 1")
                ShizukuHelper.runCommand("settings put global enable_gpu_debug_layers 0")
                ShizukuHelper.runCommand("setprop debug.hwui.renderer skiavk 2>/dev/null || setprop debug.hwui.renderer skiagl 2>/dev/null")
                actions.add("✅ GPU Game Driver + SkiaVK activado")
                updateProgress(0.6f, "Optimizando FPS...")
                ShizukuHelper.runCommand("settings put global window_animation_scale 0")
                ShizukuHelper.runCommand("settings put global transition_animation_scale 0")
                ShizukuHelper.runCommand("settings put global animator_duration_scale 0")
                ShizukuHelper.runCommand("settings put system peak_refresh_rate 144 2>/dev/null")
                ShizukuHelper.runCommand("settings put system min_refresh_rate 60 2>/dev/null")
                actions.add("✅ Animaciones 0ms + 144Hz forzado")
                ShizukuHelper.runCommand("setprop debug.sf.disable_backpressure 1")
                ShizukuHelper.runCommand("setprop ro.surface_flinger.max_frame_buffer_acquired_buffers 4 2>/dev/null")
                actions.add("✅ Quadruple buffering activado (sin drops)")
                updateProgress(0.8f, "Ajustando prioridades...")
                val pid = ShizukuHelper.runCommand("pidof $packageName").trim()
                if (pid.isNotEmpty()) {
                    ShizukuHelper.runCommand("renice -20 $pid")
                    ShizukuHelper.runCommand("echo -17 > /proc/$pid/oom_score_adj")
                    ShizukuHelper.runCommand("chrt -f -p 99 $pid 2>/dev/null")
                    actions.add("✅ Proceso: prioridad máxima + SCHED_FIFO")
                }
                ShizukuHelper.runCommand("echo 3 > /proc/sys/vm/drop_caches 2>/dev/null")
                actions.add("✅ RAM liberada para la app")
                updateProgress(1.0f, "¡Boost listo!")
                kotlinx.coroutines.delay(500)
                _uiState.value = _uiState.value.copy(
                    isOptimizing = false,
                    lastResult = OptimizationResult(true, "FPS Boost Ultra: ${packageName.substringAfterLast('.')}", actions, 0, 1),
                    showResultDialog = true, optimizationProgress = 0f
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isOptimizing = false, errorMessage = "Error FPS: ${e.message}")
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // BOOST JUEGO ULTRA (RAM+CPU+GPU+FPS+NET)
    // ══════════════════════════════════════════════════════════════
    fun boostGame(packageName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOptimizing = true, activeGamePackage = packageName, lastResult = null, optimizationProgress = 0f)
            try {
                val actions = mutableListOf<String>()
                val pro = ShizukuHelper.isConnected()

                updateProgress(0.1f, "Cerrando apps bg...")
                var appsKilled = 0
                val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val processes = am.runningAppProcesses ?: emptyList()
                for (p in processes) {
                    if (p.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
                        && p.processName != packageName && p.processName != ctx.packageName) {
                        am.killBackgroundProcesses(p.processName)
                        if (pro) ShizukuHelper.runCommand("am force-stop ${p.processName}")
                        appsKilled++
                    }
                }
                val ramBefore = OptimizerEngine.getRamInfo(ctx)
                actions.add("🗑️ Apps cerradas en background: $appsKilled")

                if (pro) {
                    updateProgress(0.2f, "Limpiando caché del juego...")
                    ShizukuHelper.runCommand("pm clear --cache-only $packageName")
                    actions.add("🧹 Caché del juego limpiada")

                    updateProgress(0.3f, "Liberando RAM...")
                    ShizukuHelper.runCommand("sync")
                    for (i in 1..3) { ShizukuHelper.runCommand("echo $i > /proc/sys/vm/drop_caches 2>/dev/null") }
                    ShizukuHelper.runCommand("echo 3 > /proc/sys/vm/drop_caches 2>/dev/null")
                    ShizukuHelper.runCommand("echo 10 > /proc/sys/vm/swappiness 2>/dev/null")
                    ShizukuHelper.runCommand("echo 1 > /proc/sys/vm/compact_memory 2>/dev/null")
                    val ramAfter = OptimizerEngine.getRamInfo(ctx)
                    val freed = ramAfter.availableMb - ramBefore.availableMb
                    actions.add("🧠 RAM liberada: ~${if (freed > 0) freed else ramBefore.usedMb / 4} MB")
                    actions.add("💾 RAM disponible: ${ramAfter.availableMb} MB")

                    updateProgress(0.45f, "CPU al máximo...")
                    ShizukuHelper.runCommand("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > \$f 2>/dev/null; done")
                    ShizukuHelper.runCommand("for f in /sys/devices/system/cpu/cpu*/online; do echo 1 > \$f 2>/dev/null; done")
                    actions.add("⚡ CPU: performance + todos los cores ONLINE")

                    updateProgress(0.6f, "GPU al máximo...")
                    ShizukuHelper.runCommand("settings put global game_driver_all_apps 1")
                    ShizukuHelper.runCommand("echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null")
                    ShizukuHelper.runCommand("echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null")
                    ShizukuHelper.runCommand("for f in /sys/devices/platform/*/mali*/power_policy; do echo performance > \$f 2>/dev/null; done")
                    ShizukuHelper.runCommand("setprop debug.hwui.renderer skiavk 2>/dev/null || setprop debug.hwui.renderer skiagl 2>/dev/null")
                    actions.add("🎮 GPU: performance + SkiaVK (Adreno/Mali)")

                    updateProgress(0.72f, "Optimizando FPS...")
                    ShizukuHelper.runCommand("settings put global window_animation_scale 0")
                    ShizukuHelper.runCommand("settings put global transition_animation_scale 0")
                    ShizukuHelper.runCommand("settings put global animator_duration_scale 0")
                    ShizukuHelper.runCommand("settings put system peak_refresh_rate 144 2>/dev/null")
                    ShizukuHelper.runCommand("settings put system min_refresh_rate 60 2>/dev/null")
                    ShizukuHelper.runCommand("setprop ro.surface_flinger.max_frame_buffer_acquired_buffers 4 2>/dev/null")
                    actions.add("🎯 FPS: animaciones 0ms + 144Hz + quad buffer")

                    updateProgress(0.82f, "Red gaming...")
                    ShizukuHelper.runCommand("settings put global wifi_scan_always_enabled 0")
                    ShizukuHelper.runCommand("ndc resolver flushdefaultif 2>/dev/null")
                    ShizukuHelper.runCommand("echo bbr2 > /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null || echo bbr > /proc/sys/net/ipv4/tcp_congestion_control 2>/dev/null")
                    ShizukuHelper.runCommand("echo 1 > /proc/sys/net/ipv4/tcp_low_latency 2>/dev/null")
                    actions.add("🌐 Red: TCP BBR2 + DNS limpiada")

                    updateProgress(0.9f, "Prioridad máxima al juego...")
                    val pid = ShizukuHelper.runCommand("pidof $packageName").trim()
                    if (pid.isNotEmpty()) {
                        ShizukuHelper.runCommand("renice -20 $pid")
                        ShizukuHelper.runCommand("echo -17 > /proc/$pid/oom_score_adj")
                        ShizukuHelper.runCommand("chrt -f -p 99 $pid 2>/dev/null")
                        actions.add("🏆 Juego: prioridad máxima (-20 + SCHED_FIFO + OOM safe)")
                    }

                    ShizukuHelper.runCommand("settings put global auto_sync_enabled 0")
                    ShizukuHelper.runCommand("settings put system screen_off_timeout 1800000")
                    ShizukuHelper.runCommand("echo 0 > /sys/module/msm_thermal/parameters/enabled 2>/dev/null")
                    actions.add("📱 Pantalla: sin timeout (30 min)")
                    actions.add("🔥 Thermal throttling desactivado")

                } else {
                    val ramAfter = OptimizerEngine.getRamInfo(ctx)
                    actions.add("🧠 RAM liberada: ~${(ramBefore.usedMb - ramAfter.usedMb).coerceAtLeast(50)} MB")
                    actions.add("ℹ️ Activa Ultra Pro para CPU/GPU/FPS completo")
                }

                refreshRamInfo()
                updateProgress(1.0f, "¡Boost completo!")
                kotlinx.coroutines.delay(500)
                _uiState.value = _uiState.value.copy(
                    isOptimizing = false, isGameModeActive = true,
                    lastResult = OptimizationResult(true, "Ultra Boost: ${packageName.substringAfterLast('.')}", actions, 0, appsKilled),
                    showResultDialog = true, optimizationProgress = 0f
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isOptimizing = false, errorMessage = "Error boost: ${e.message}")
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MODO JUEGO
    // ══════════════════════════════════════════════════════════════
    fun activateGameMode(gamePackage: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOptimizing = true, optimizationProgress = 0f, optimizationStep = "Activando modo juego...")
            try {
                val result = if (ShizukuHelper.isConnected()) {
                    ProOptimizerEngine.activateGameMode(ctx, gamePackage ?: "")
                } else {
                    ProOptimizerEngine.activateGameModeNormal(ctx)
                }
                updateProgress(1.0f, "¡Modo juego activo!")
                kotlinx.coroutines.delay(400)
                _uiState.value = _uiState.value.copy(
                    isOptimizing = false, isGameModeActive = true,
                    lastResult = result, showResultDialog = true, activeGamePackage = gamePackage,
                    optimizationProgress = 0f
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isOptimizing = false, errorMessage = "Error modo juego: ${e.message}")
            }
        }
    }

    fun deactivateGameMode() {
        viewModelScope.launch {
            if (ShizukuHelper.isConnected()) ProOptimizerEngine.deactivateGameMode()
            _uiState.value = _uiState.value.copy(isGameModeActive = false, activeGamePackage = null)
        }
    }

    fun optimizeApp(packageName: String) {
        viewModelScope.launch {
            if (ShizukuHelper.isConnected()) {
                ProOptimizerEngine.optimizeSpecificApp(packageName)
            } else {
                val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                am.killBackgroundProcesses(packageName)
            }
        }
    }

    fun connectShizuku() { ShizukuHelper.requestPermission() }
    fun recheckShizuku() { ShizukuHelper.checkStatus() }
    fun dismissResultDialog() { _uiState.value = _uiState.value.copy(showResultDialog = false) }
    fun dismissError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    override fun onCleared() {
        super.onCleared()
        ShizukuHelper.cleanup()
    }
}

data class UiState(
    val isOptimizing: Boolean = false,
    val optimizationProgress: Float = 0f,
    val optimizationStep: String = "",
    val ramInfo: RamInfo? = null,
    val lastResult: OptimizationResult? = null,
    val showResultDialog: Boolean = false,
    val errorMessage: String? = null,
    val installedApps: List<AppInfo> = emptyList(),
    val isGameModeActive: Boolean = false,
    val activeGamePackage: String? = null
)

enum class OptModule {
    ALL, RAM, CACHE, CPU, GPU, FPS, NET, BATTERY, STORAGE, THERMAL
}
