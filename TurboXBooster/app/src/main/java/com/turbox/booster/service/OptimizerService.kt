package com.turbox.booster.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.turbox.booster.MainActivity
import com.turbox.booster.R
import com.turbox.booster.TurboXApp
import com.turbox.booster.utils.SystemUtils
import kotlinx.coroutines.*

class OptimizerService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification("Monitoreando sistema..."))
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoring() {
        monitorJob = scope.launch {
            while (isActive) {
                try {
                    val ram = SystemUtils.getRamInfo(this@OptimizerService)
                    val cpu = SystemUtils.getCpuUsage()
                    val temp = SystemUtils.getCpuTemperature()
                    val score = SystemUtils.calculateSystemScore(this@OptimizerService)

                    val status = buildStatusString(cpu, ram.usagePercent, temp, score)
                    updateNotification(status)

                    // Alertas automáticas
                    if (temp > 50f) {
                        sendAlert("⚠️ Temperatura alta: ${temp.toInt()}°C")
                    }
                    if (ram.usagePercent > 90f) {
                        sendAlert("⚠️ RAM crítica: ${ram.usagePercent.toInt()}%")
                        // Auto-GC
                        SystemUtils.triggerGarbageCollection()
                    }

                    delay(5000) // Actualizar cada 5 segundos
                } catch (e: Exception) {
                    delay(10000)
                }
            }
        }
    }

    private fun buildStatusString(cpu: Float, ram: Float, temp: Float, score: Int): String {
        return "CPU: ${cpu.toInt()}% | RAM: ${ram.toInt()}% | ${temp.toInt()}°C | Score: $score"
    }

    private fun buildNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TurboXApp.CHANNEL_OPTIMIZER)
            .setContentTitle("TurboX Booster")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_optimizer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm?.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private fun sendAlert(message: String) {
        val nm = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, TurboXApp.CHANNEL_ALERTS)
            .setContentTitle("TurboX Booster")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_optimizer)
            .setAutoCancel(true)
            .build()
        nm?.notify(ALERT_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        scope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ALERT_NOTIFICATION_ID = 2
    }
}
