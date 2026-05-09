package com.optimizer.pro

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import rikka.shizuku.Shizuku

object ShizukuHelper {

    private const val TAG = "ShizukuHelper"
    private const val SHIZUKU_PERMISSION_CODE = 1001

    private val _status = MutableStateFlow(ShizukuStatus.CHECKING)
    val status: StateFlow<ShizukuStatus> = _status

    private var shizukuService: IShizukuService? = null

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName("com.optimizer.pro", ShizukuServiceImpl::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(false)
        .version(BuildConfig.VERSION_CODE)

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            if (binder != null && binder.isBinderAlive) {
                shizukuService = IShizukuService.Stub.asInterface(binder)
                _status.value = ShizukuStatus.CONNECTED
            } else {
                _status.value = ShizukuStatus.ERROR
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            shizukuService = null
            _status.value = ShizukuStatus.DISCONNECTED
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkAndConnect()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        shizukuService = null
        _status.value = ShizukuStatus.NOT_RUNNING
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            bindService()
        } else {
            _status.value = ShizukuStatus.PERMISSION_DENIED
        }
    }

    fun initialize() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        checkStatus()
    }

    fun cleanup() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        try {
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, false)
        } catch (e: Exception) { }
    }

    fun checkStatus() {
        try {
            _status.value = when {
                !Shizuku.pingBinder()  -> ShizukuStatus.NOT_RUNNING
                !hasPermission()       -> ShizukuStatus.NO_PERMISSION
                shizukuService != null -> ShizukuStatus.CONNECTED
                else -> { bindService(); ShizukuStatus.BINDING }
            }
        } catch (e: Exception) {
            _status.value = ShizukuStatus.NOT_RUNNING
        }
    }

    private fun checkAndConnect() {
        try {
            if (!Shizuku.pingBinder()) { _status.value = ShizukuStatus.NOT_RUNNING; return }
            if (hasPermission()) bindService() else _status.value = ShizukuStatus.NO_PERMISSION
        } catch (e: Exception) {
            _status.value = ShizukuStatus.ERROR
        }
    }

    fun hasPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) { false }
    }

    fun isConnected(): Boolean = shizukuService != null && _status.value == ShizukuStatus.CONNECTED

    fun requestPermission() {
        try {
            if (!Shizuku.pingBinder()) { _status.value = ShizukuStatus.NOT_RUNNING; return }
            if (hasPermission()) bindService() else Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
        } catch (e: Exception) {
            _status.value = ShizukuStatus.NOT_RUNNING
        }
    }

    private fun bindService() {
        try {
            _status.value = ShizukuStatus.BINDING
            Shizuku.bindUserService(userServiceArgs, userServiceConnection)
        } catch (e: Exception) {
            _status.value = ShizukuStatus.ERROR
        }
    }

    fun runCommand(command: String): String {
        val service = shizukuService
        return if (service != null) {
            try {
                service.runCommand(command)
            } catch (e: Exception) {
                Log.e(TAG, "Error runCommand: $command", e)
                ""
            }
        } else {
            Log.w(TAG, "Shizuku no conectado, comando ignorado: $command")
            ""
        }
    }

    fun runCommands(commands: Array<String>): String {
        val sb = StringBuilder()
        commands.forEach { sb.appendLine(runCommand(it)) }
        return sb.toString()
    }

    fun clearAppCache(packageName: String): Boolean {
        return try {
            val result = runCommand("pm clear --cache-only $packageName")
            !result.contains("ERROR")
        } catch (e: Exception) { false }
    }

    fun killProcess(packageName: String): Boolean {
        return try { runCommand("am force-stop $packageName"); true }
        catch (e: Exception) { false }
    }
}

enum class ShizukuStatus {
    CHECKING, NOT_INSTALLED, NOT_RUNNING, NO_PERMISSION,
    PERMISSION_DENIED, BINDING, CONNECTED, READY, DISCONNECTED, ERROR
}
