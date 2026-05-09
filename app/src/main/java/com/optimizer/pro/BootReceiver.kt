package com.optimizer.pro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Boot completado, ejecutando optimización automática")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    OptimizerEngine.runNormalOptimization(context)
                    Log.d("BootReceiver", "Optimización al inicio completada")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error en optimización al inicio", e)
                }
            }
        }
    }
}
