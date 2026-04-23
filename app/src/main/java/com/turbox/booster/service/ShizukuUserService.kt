package com.turbox.booster.service

import android.app.ActivityManager
import android.content.Context
import android.os.IBinder
import android.os.Process
import com.turbox.booster.IUserService

class ShizukuUserService : IUserService.Stub() {

    override fun destroy() {
        System.exit(0)
    }

    override fun exit() {
        System.exit(0)
    }

    override fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.waitFor()
            if (output.isNotEmpty()) output.trim() else error.trim()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    override fun setAnimationScale(scale: Float) {
        executeCommand("settings put global window_animation_scale $scale")
        executeCommand("settings put global transition_animation_scale $scale")
        executeCommand("settings put global animator_duration_scale $scale")
    }

    override fun killBackgroundProcesses(packageName: String) {
        executeCommand("am force-stop $packageName")
    }

    override fun killAllBackgroundProcesses() {
        executeCommand("am kill-all")
        executeCommand("am compact all")
    }

    override fun trimAllCaches() {
        executeCommand("pm trim-caches 999999999999")
        executeCommand("sync && echo 3 > /proc/sys/vm/drop_caches")
    }

    override fun setGpuRendering(force: Boolean) {
        val v = if (force) "1" else "0"
        executeCommand("setprop debug.egl.hw $v")
        executeCommand("setprop debug.sf.hw $v")
        executeCommand("setprop persist.sys.ui.hw $v")
        executeCommand("setprop debug.hwui.renderer ${if (force) "skiagl" else "skiagl"}")
    }

    override fun setWindowAnimationScale(scale: Float) {
        executeCommand("settings put global window_animation_scale $scale")
    }

    override fun setTransitionAnimationScale(scale: Float) {
        executeCommand("settings put global transition_animation_scale $scale")
    }

    override fun setAnimatorDurationScale(scale: Float) {
        executeCommand("settings put global animator_duration_scale $scale")
    }

    override fun setDns(dns1: String, dns2: String) {
        executeCommand("ndc resolver setnetdns 100 \"\" $dns1 $dns2")
        executeCommand("setprop net.dns1 $dns1")
        executeCommand("setprop net.dns2 $dns2")
    }

    override fun setWifiOptimization(enabled: Boolean) {
        val v = if (enabled) "1" else "0"
        executeCommand("settings put global wifi_scan_throttle_enabled $v")
        executeCommand("settings put global wifi_watchdog_on $v")
    }

    override fun setGameMode(packageName: String, mode: Int) {
        // mode: 0=unsupported, 1=standard, 2=performance, 3=battery
        executeCommand("cmd game mode $mode $packageName")
        executeCommand("cmd game set --mode $mode $packageName")
    }

    override fun clearThermalThrottle() {
        executeCommand("cmd thermalservice override-status -1")
        executeCommand("dumpsys thermalservice override-status -1")
    }

    override fun getDetailedCpuInfo(): String {
        return executeCommand("cat /proc/cpuinfo")
    }

    override fun getDetailedMemInfo(): String {
        return executeCommand("cat /proc/meminfo")
    }

    override fun getRunningProcesses(): String {
        return executeCommand("ps -A -o PID,NAME,RSS,%CPU,PRIORITY | head -50")
    }

    override fun setPerformanceMode(mode: Int) {
        when (mode) {
            0 -> { // Battery saver
                executeCommand("settings put global low_power 1")
                executeCommand("cmd power set-mode 1")
            }
            1 -> { // Balanced
                executeCommand("settings put global low_power 0")
                executeCommand("cmd power set-mode 0")
            }
            2 -> { // Performance
                executeCommand("settings put global low_power 0")
                executeCommand("cmd power set-mode 2")
                executeCommand("setprop sys.perf.profile 1")
                executeCommand("settings put global development_settings_enabled 1")
                executeCommand("settings put global force_hw_ui true")
            }
        }
    }

    override fun setBackgroundProcessLimit(limit: Int) {
        executeCommand("settings put global activity_manager_constants max_cached_processes=$limit")
        executeCommand("settings put global background_activity_starts_enabled ${if (limit > 0) "true" else "false"}")
    }

    override fun setPackageEnabled(packageName: String, enabled: Boolean) {
        if (enabled) {
            executeCommand("pm enable --user 0 $packageName")
        } else {
            executeCommand("pm disable-user --user 0 $packageName")
        }
    }
}
