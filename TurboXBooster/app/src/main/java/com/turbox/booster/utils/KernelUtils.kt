package com.turbox.booster.utils

import java.io.File

/**
 * Lectura de parámetros del kernel desde /proc y /sys (sin root, solo lectura).
 * Las escrituras privilegiadas se hacen via ShizukuHelper.
 */
object KernelUtils {

    data class KernelInfo(
        val version: String,
        val hostname: String,
        val cpuScheduler: String,
        val vmSwappiness: Int,
        val vmDirtyRatio: Int,
        val ioScheduler: String,
        val tcpCongestion: String,
        val anonHugepages: Boolean
    )

    fun getKernelVersion(): String {
        return try {
            File("/proc/version").readText().trim()
                .substringBefore("(").trim()
        } catch (e: Exception) { android.os.Build.DISPLAY }
    }

    fun getHostname(): String {
        return try {
            File("/proc/sys/kernel/hostname").readText().trim()
        } catch (e: Exception) { android.os.Build.DEVICE }
    }

    fun getVmSwappiness(): Int {
        return try {
            File("/proc/sys/vm/swappiness").readText().trim().toInt()
        } catch (e: Exception) { -1 }
    }

    fun getVmDirtyRatio(): Int {
        return try {
            File("/proc/sys/vm/dirty_ratio").readText().trim().toInt()
        } catch (e: Exception) { -1 }
    }

    fun getIoScheduler(device: String = "mmcblk0"): String {
        return try {
            val content = File("/sys/block/$device/queue/scheduler").readText().trim()
            // Formato: "noop [cfq] deadline" — extraer el activo
            val match = Regex("\\[(.+?)]").find(content)
            match?.groupValues?.get(1) ?: content
        } catch (e: Exception) {
            try {
                val content = File("/sys/block/sda/queue/scheduler").readText().trim()
                Regex("\\[(.+?)]").find(content)?.groupValues?.get(1) ?: content
            } catch (e2: Exception) { "unknown" }
        }
    }

    fun getTcpCongestion(): String {
        return try {
            File("/proc/sys/net/ipv4/tcp_congestion_control").readText().trim()
        } catch (e: Exception) { "unknown" }
    }

    fun getAvailableIoSchedulers(device: String = "mmcblk0"): List<String> {
        return try {
            val content = File("/sys/block/$device/queue/scheduler").readText().trim()
            content.replace("[", "").replace("]", "").split(" ").filter { it.isNotBlank() }
        } catch (e: Exception) { emptyList() }
    }

    fun getAvailableTcpCongestions(): List<String> {
        return try {
            File("/proc/sys/net/ipv4/tcp_available_congestion_control")
                .readText().trim().split(" ")
        } catch (e: Exception) { listOf("cubic", "bbr", "reno") }
    }

    fun getMemoryPressure(): String {
        return try {
            val memFree = readMemInfoValue("MemFree")
            val memAvail = readMemInfoValue("MemAvailable")
            val cached = readMemInfoValue("Cached")
            "Libre: ${memFree/1024}MB | Disponible: ${memAvail/1024}MB | Cached: ${cached/1024}MB"
        } catch (e: Exception) { "N/A" }
    }

    private fun readMemInfoValue(key: String): Long {
        return try {
            File("/proc/meminfo").readLines()
                .firstOrNull { it.startsWith(key) }
                ?.replace("[^\\d]".toRegex(), "")?.toLong() ?: 0L
        } catch (e: Exception) { 0L }
    }

    fun getUptimeSeconds(): Long {
        return try {
            File("/proc/uptime").readText().trim()
                .split(" ").firstOrNull()?.toDouble()?.toLong() ?: 0L
        } catch (e: Exception) { 0L }
    }

    fun getFormattedUptime(): String {
        val total = getUptimeSeconds()
        val days = total / 86400
        val hours = (total % 86400) / 3600
        val minutes = (total % 3600) / 60
        return when {
            days > 0 -> "${days}d ${hours}h ${minutes}m"
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    fun getLoadAverage(): Triple<Float, Float, Float> {
        return try {
            val parts = File("/proc/loadavg").readText().trim().split(" ")
            Triple(
                parts[0].toFloat(),
                parts[1].toFloat(),
                parts[2].toFloat()
            )
        } catch (e: Exception) { Triple(0f, 0f, 0f) }
    }

    fun getOpenFileDescriptors(): Pair<Long, Long> {
        return try {
            val parts = File("/proc/sys/fs/file-nr").readText().trim().split("\t")
            Pair(parts[0].trim().toLong(), parts[2].trim().toLong())
        } catch (e: Exception) { Pair(0L, 0L) }
    }

    // ─── TWEAKS VIA SHIZUKU ──────────────────────────────────────────────────

    fun applyLowLatencyKernelTweaks() {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) return
        // VM tweaks para gaming (menos swapping, más RAM en app activa)
        ShizukuHelper.executeCommand("echo 10 > /proc/sys/vm/swappiness")
        ShizukuHelper.executeCommand("echo 10 > /proc/sys/vm/dirty_ratio")
        ShizukuHelper.executeCommand("echo 5 > /proc/sys/vm/dirty_background_ratio")
        ShizukuHelper.executeCommand("echo 3000 > /proc/sys/vm/dirty_expire_centisecs")
        // CPU scheduler: optimizar quantum de tiempo
        ShizukuHelper.executeCommand("echo 1000000 > /proc/sys/kernel/sched_latency_ns")
        ShizukuHelper.executeCommand("echo 500000 > /proc/sys/kernel/sched_min_granularity_ns")
        ShizukuHelper.executeCommand("echo 0 > /proc/sys/kernel/sched_child_runs_first")
    }

    fun applyNetworkKernelTweaks() {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) return
        // TCP buffers más grandes para gaming/streaming
        ShizukuHelper.executeCommand("echo 4096 87380 16777216 > /proc/sys/net/ipv4/tcp_rmem")
        ShizukuHelper.executeCommand("echo 4096 65536 16777216 > /proc/sys/net/ipv4/tcp_wmem")
        ShizukuHelper.executeCommand("echo 1 > /proc/sys/net/ipv4/tcp_low_latency")
        ShizukuHelper.executeCommand("echo 1 > /proc/sys/net/ipv4/tcp_fastopen")
        ShizukuHelper.executeCommand("echo bbr > /proc/sys/net/ipv4/tcp_congestion_control")
    }

    fun setIoScheduler(scheduler: String, device: String = "mmcblk0") {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) return
        ShizukuHelper.executeCommand("echo $scheduler > /sys/block/$device/queue/scheduler")
        ShizukuHelper.executeCommand("echo $scheduler > /sys/block/sda/queue/scheduler")
    }

    fun setReadAheadKb(kb: Int = 2048, device: String = "mmcblk0") {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) return
        ShizukuHelper.executeCommand("echo $kb > /sys/block/$device/queue/read_ahead_kb")
        ShizukuHelper.executeCommand("echo $kb > /sys/block/sda/queue/read_ahead_kb")
    }

    fun getKernelInfo(): KernelInfo {
        return KernelInfo(
            version = getKernelVersion(),
            hostname = getHostname(),
            cpuScheduler = SystemUtils.getCpuGovernor(),
            vmSwappiness = getVmSwappiness(),
            vmDirtyRatio = getVmDirtyRatio(),
            ioScheduler = getIoScheduler(),
            tcpCongestion = getTcpCongestion(),
            anonHugepages = false
        )
    }
}
