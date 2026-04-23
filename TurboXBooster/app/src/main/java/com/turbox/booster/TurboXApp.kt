package com.turbox.booster

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class TurboXApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Canal principal de optimización
            NotificationChannel(
                CHANNEL_OPTIMIZER,
                "TurboX Booster",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Servicio de optimización en segundo plano"
                setShowBadge(false)
                manager.createNotificationChannel(this)
            }

            // Canal de alertas
            NotificationChannel(
                CHANNEL_ALERTS,
                "Alertas del sistema",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas de temperatura y rendimiento"
                manager.createNotificationChannel(this)
            }
        }
    }

    companion object {
        lateinit var instance: TurboXApp
            private set
        const val CHANNEL_OPTIMIZER = "turbox_booster"
        const val CHANNEL_ALERTS = "turbox_alerts"
    }
}
