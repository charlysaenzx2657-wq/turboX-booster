package com.optimizer.pro

import android.util.Log
import kotlin.system.exitProcess

/**
 * Este servicio corre en el contexto de shell (uid=2000) via Shizuku.
 * Puede ejecutar comandos con permisos de ADB.
 */
class ShizukuServiceImpl : IShizukuService.Stub() {

    companion object {
        private const val TAG = "ShizukuServiceImpl"
    }

    override fun runCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.waitFor()
            if (error.isNotBlank()) "OUTPUT: $output\nERROR: $error"
            else output
        } catch (e: Exception) {
            Log.e(TAG, "Error running command: $command", e)
            "ERROR: ${e.message}"
        }
    }

    override fun runCommands(commands: Array<out String>): String {
        val results = StringBuilder()
        commands.forEach { cmd ->
            results.appendLine(">>> $cmd")
            results.appendLine(runCommand(cmd))
        }
        return results.toString()
    }

    override fun clearAppCache(packageName: String): Boolean {
        return try {
            val result = runCommand("pm clear --cache-only $packageName")
            !result.contains("ERROR")
        } catch (e: Exception) {
            false
        }
    }

    override fun killProcess(packageName: String): Boolean {
        return try {
            runCommand("am force-stop $packageName")
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun isAlive(): Boolean = true

    override fun destroy() {
        exitProcess(0)
    }
}
