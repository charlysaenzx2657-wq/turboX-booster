package com.turbox.booster.utils

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Debug
import android.os.Environment
import android.os.StatFs
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.RandomAccessFile
import kotlin.math.roundToInt

object SystemUtils {

    // ─── CPU ─────────────────────────────────────────────────────────────────

    data class CpuStats(
        val usagePercent: Float,
        val coreCount: Int,
        val frequencies: List<Long>,  // kHz
        val maxFrequencies: List<Long>,
        val governor: String,
        val architecture: String
    )

    private var prevIdle = 0L
    private var prevTotal = 0L

    fun getCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()
            val parts = load.split("\\s+".toRegex()).drop(1)
            val idle = parts[3].toLong() + parts[4].toLong()
            val total = parts.take(8).map { it.toLong() }.sum()
            val diffIdle = idle - prevIdle
            val diffTotal = total - prevTotal
            prevIdle = idle
            prevTotal = total
            if (diffTotal == 0L) 0f
            else ((diffTotal - diffIdle).toFloat() / diffTotal * 100).coerceIn(0f, 100f)
        } catch (e: Exception) { 0f }
    }

    fun getCpuCoreCount(): Int = Runtime.getRuntime().availableProcessors()

    fun getCpuFrequencies(): List<Long> {
        val freqs = mutableListOf<Long>()
        for (i in 0 until getCpuCoreCount()) {
            try {
                val file = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                if (file.exists()) {
                    freqs.add(file.readText().trim().toLong())
                } else freqs.add(0L)
            } catch (e: Exception) { freqs.add(0L) }
        }
        return freqs
    }

    fun getCpuMaxFrequencies(): List<Long> {
        val freqs = mutableListOf<Long>()
        for (i in 0 until getCpuCoreCount()) {
            try {
                val file = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
                if (file.exists()) freqs.add(file.readText().trim().toLong())
                else freqs.add(0L)
            } catch (e: Exception) { freqs.add(0L) }
        }
        return freqs
    }

    fun getCpuGovernor(): String {
        return try {
            val file = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
            if (file.exists()) file.readText().trim() else "unknown"
        } catch (e: Exception) { "unknown" }
    }

    fun getCpuArchitecture(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/cpuinfo"))
            var arch = "unknown"
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("Hardware") || line!!.startsWith("Processor") || line!!.startsWith("model name")) {
                    arch = line!!.substringAfter(":").trim()
                    break
                }
            }
            reader.close()
            arch
        } catch (e: Exception) { android.os.Build.HARDWARE }
    }

    fun getCpuTemperature(): Float {
        val thermalFiles = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/kernel/debug/tegra_thermal/cpu_temp"
        )
        for (path in thermalFiles) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val raw = file.readText().trim().toLong()
                    return if (raw > 1000) raw / 1000f else raw.toFloat()
                }
            } catch (e: Exception) { continue }
        }
        return 0f
    }

    // ─── RAM ─────────────────────────────────────────────────────────────────

    data class RamInfo(
        val totalMb: Long,
        val availableMb: Long,
        val usedMb: Long,
        val usagePercent: Float,
        val threshold: Long
    )

    fun getRamInfo(context: Context): RamInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val total = memInfo.totalMem / 1024 / 1024
        val available = memInfo.availMem / 1024 / 1024
        val used = total - available
        val percent = if (total > 0) (used.toFloat() / total * 100) else 0f
        return RamInfo(total, available, used, percent, memInfo.threshold / 1024 / 1024)
    }

    fun getSwapInfo(): Pair<Long, Long> {
        return try {
            val reader = BufferedReader(FileReader("/proc/meminfo"))
            var swapTotal = 0L
            var swapFree = 0L
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                when {
                    line!!.startsWith("SwapTotal:") -> swapTotal = line!!.replace("[^\\d]".toRegex(), "").toLong() / 1024
                    line!!.startsWith("SwapFree:") -> swapFree = line!!.replace("[^\\d]".toRegex(), "").toLong() / 1024
                }
            }
            reader.close()
            Pair(swapTotal, swapFree)
        } catch (e: Exception) { Pair(0L, 0L) }
    }

    // ─── ALMACENAMIENTO ──────────────────────────────────────────────────────

    data class StorageInfo(
        val totalGb: Float,
        val freeGb: Float,
        val usedGb: Float,
        val usagePercent: Float
    )

    fun getStorageInfo(): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val total = (stat.blockCountLong * blockSize) / 1024f / 1024f / 1024f
        val free = (stat.availableBlocksLong * blockSize) / 1024f / 1024f / 1024f
        val used = total - free
        val percent = if (total > 0) (used / total * 100) else 0f
        return StorageInfo(total, free, used, percent)
    }

    // ─── BATERÍA ─────────────────────────────────────────────────────────────

    data class BatteryInfo(
        val level: Int,
        val temperature: Float,
        val voltage: Float,
        val status: String,
        val health: String,
        val isCharging: Boolean
    )

    fun getBatteryInfo(context: Context): BatteryInfo {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val temp = try {
            val file = File("/sys/class/power_supply/battery/temp")
            if (file.exists()) file.readText().trim().toFloat() / 10f
            else 0f
        } catch (e: Exception) { 0f }
        val voltage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_VOLTAGE) / 1000f
        val isCharging = bm.isCharging
        val status = if (isCharging) "Cargando" else "Descargando"
        return BatteryInfo(level, temp, voltage, status, "Bueno", isCharging)
    }

    // ─── MEMORIA NATIVA ──────────────────────────────────────────────────────

    fun triggerGarbageCollection() {
        Runtime.getRuntime().gc()
        System.gc()
        System.runFinalization()
    }

    fun getNativeHeapUsedMb(): Float {
        return Debug.getNativeHeapAllocatedSize() / 1024f / 1024f
    }

    // ─── NETWORK ─────────────────────────────────────────────────────────────

    fun getNetworkLatency(): Long {
        return try {
            val start = System.currentTimeMillis()
            val process = Runtime.getRuntime().exec("ping -c 1 -W 1 8.8.8.8")
            process.waitFor()
            System.currentTimeMillis() - start
        } catch (e: Exception) { -1L }
    }

    fun getRxTxBytes(): Pair<Long, Long> {
        return try {
            val rxFile = File("/proc/net/dev")
            var rx = 0L; var tx = 0L
            rxFile.readLines().drop(2).forEach { line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size > 9 && !parts[0].startsWith("lo")) {
                    rx += parts[1].toLongOrNull() ?: 0L
                    tx += parts[9].toLongOrNull() ?: 0L
                }
            }
            Pair(rx, tx)
        } catch (e: Exception) { Pair(0L, 0L) }
    }

    // ─── SYSTEM SCORE ────────────────────────────────────────────────────────

    fun calculateSystemScore(context: Context): Int {
        val ram = getRamInfo(context)
        val cpu = getCpuUsage()
        val temp = getCpuTemperature()
        val storage = getStorageInfo()

        var score = 100
        score -= (ram.usagePercent / 5).roundToInt()
        score -= (cpu / 5).roundToInt()
        if (temp > 45f) score -= ((temp - 45f) * 2).roundToInt()
        if (storage.usagePercent > 80f) score -= ((storage.usagePercent - 80f) / 2).roundToInt()

        return score.coerceIn(0, 100)
    }
}
