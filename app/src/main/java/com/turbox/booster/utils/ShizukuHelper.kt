package com.turbox.booster.utils

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.turbox.booster.IUserService
import com.turbox.booster.service.ShizukuUserService
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess

object ShizukuHelper {

    const val SHIZUKU_REQUEST_CODE = 1001

    private var userService: IUserService? = null
    private var isAvailable = false
    private var hasPermission = false
    private val permissionListeners = mutableListOf<(Boolean) -> Unit>()

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName("com.turbox.booster", ShizukuUserService::class.java.name)
    ).daemon(false).processNameSuffix("service").debuggable(false).version(1)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            userService = IUserService.Stub.asInterface(binder)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
        }
    }

    fun init() {
        try {
            isAvailable = Shizuku.pingBinder()
            if (isAvailable) {
                hasPermission = checkPermission()
            }
        } catch (e: Exception) {
            isAvailable = false
        }
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) { false }
    }

    fun hasShizukuPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) { false }
    }

    fun requestPermission() {
        try {
            Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
        } catch (e: Exception) {}
    }

    fun onPermissionResult(granted: Boolean) {
        hasPermission = granted
        permissionListeners.forEach { it(granted) }
        if (granted) bindService()
    }

    fun addPermissionListener(listener: (Boolean) -> Unit) {
        permissionListeners.add(listener)
    }

    fun removePermissionListener(listener: (Boolean) -> Unit) {
        permissionListeners.remove(listener)
    }

    private fun checkPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) { false }
    }

    fun bindService() {
        if (!isShizukuAvailable() || !hasShizukuPermission()) return
        try {
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        } catch (e: Exception) {}
    }

    fun unbindService() {
        try {
            Shizuku.unbindUserService(userServiceArgs, serviceConnection, true)
        } catch (e: Exception) {}
    }

    fun getUserService(): IUserService? = userService

    // ─── EJECUCIÓN DIRECTA DE COMANDOS ───────────────────────────────────────

    fun executeCommand(command: String): String {
        // Primero intenta via UserService (más estable)
        userService?.let {
            return try { it.executeCommand(command) } catch (e: Exception) { "" }
        }

        // Fallback: ShizukuRemoteProcess directamente
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.waitFor()
            if (output.isNotEmpty()) output else error
        } catch (e: Exception) { "Error: ${e.message}" }
    }

    // ─── OPTIMIZACIONES VIA SHIZUKU ──────────────────────────────────────────

    fun disableAnimations() {
        executeCommand("settings put global window_animation_scale 0")
        executeCommand("settings put global transition_animation_scale 0")
        executeCommand("settings put global animator_duration_scale 0")
    }

    fun setAnimations(scale: Float) {
        executeCommand("settings put global window_animation_scale $scale")
        executeCommand("settings put global transition_animation_scale $scale")
        executeCommand("settings put global animator_duration_scale $scale")
    }

    fun forceGpuRendering(enable: Boolean) {
        val value = if (enable) "1" else "0"
        executeCommand("settings put global gpu_debug_layers_enable $value")
        executeCommand("setprop debug.hwui.renderer skiagl")
    }

    fun killAllBackgroundProcesses() {
        executeCommand("am kill-all")
    }

    fun forceStopApp(packageName: String) {
        executeCommand("am force-stop $packageName")
    }

    fun trimAllCaches() {
        executeCommand("pm trim-caches 999999999999")
    }

    fun setGameModePerformance(packageName: String) {
        executeCommand("cmd game mode performance $packageName")
        executeCommand("cmd game set --mode 2 $packageName")
    }

    fun setGameModeBattery(packageName: String) {
        executeCommand("cmd game mode battery $packageName")
    }

    fun setBackgroundProcessLimit(limit: Int) {
        // 0=standard, 1=no background, 2=at most 1, 3=at most 2, 4=at most 3, 5=at most 4
        executeCommand("settings put global activity_manager_constants max_cached_processes=$limit")
    }

    fun setWifiAggressiveScan(enable: Boolean) {
        val value = if (enable) "1" else "0"
        executeCommand("settings put global wifi_scan_throttle_enabled $value")
    }

    fun enableDeveloperOptions() {
        executeCommand("settings put global development_settings_enabled 1")
    }

    fun setHardwareAcceleration(enable: Boolean) {
        val value = if (enable) "true" else "false"
        executeCommand("setprop debug.hwui.use_vulkan $value")
    }

    fun disableBatterySaver() {
        executeCommand("settings put global low_power 0")
        executeCommand("dumpsys battery unplug")
    }

    fun clearThermalThrottle() {
        executeCommand("cmd thermalservice override-status -1")
    }

    fun setPerformanceMode() {
        executeCommand("cmd power set-mode 2")
        executeCommand("setprop sys.perf.profile 1")
    }

    fun setTcpOptimizations() {
        executeCommand("settings put global tcp_default_init_rwnd 60")
        executeCommand("settings put global network_scoring_enabled 0")
    }

    fun optimizeGpu() {
        executeCommand("setprop debug.egl.hw 1")
        executeCommand("setprop debug.sf.hw 1")
        executeCommand("setprop persist.sys.ui.hw 1")
    }

    fun setDns(primary: String, secondary: String) {
        executeCommand("ndc resolver setnetdns 100 \"\" $primary $secondary")
        executeCommand("settings put global private_dns_mode hostname")
        executeCommand("settings put global private_dns_specifier \"\"")
    }

    fun disablePackage(packageName: String) {
        executeCommand("pm disable-user --user 0 $packageName")
    }

    fun enablePackage(packageName: String) {
        executeCommand("pm enable $packageName")
    }

    fun getRunningAppsInfo(): String {
        return executeCommand("dumpsys activity processes | grep ProcessRecord")
    }

    fun clearAllAppData(packageName: String) {
        executeCommand("pm clear $packageName")
    }

    fun setScreenDensity(dpi: Int) {
        executeCommand("wm density $dpi")
    }

    fun resetScreenDensity() {
        executeCommand("wm density reset")
    }

    fun setScreenResolution(width: Int, height: Int) {
        executeCommand("wm size ${width}x${height}")
    }

    fun resetScreenResolution() {
        executeCommand("wm size reset")
    }
}
