package com.turbox.booster.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {

    private const val PREFS_NAME = "turbox_prefs"

    // Keys
    private const val KEY_AUTO_OPTIMIZE = "auto_optimize"
    private const val KEY_OPTIMIZE_ON_BOOT = "optimize_on_boot"
    private const val KEY_ANIMATION_SCALE = "animation_scale"
    private const val KEY_PERFORMANCE_PROFILE = "performance_profile"
    private const val KEY_MONITOR_ENABLED = "monitor_enabled"
    private const val KEY_TEMP_ALERT_THRESHOLD = "temp_alert_threshold"
    private const val KEY_RAM_ALERT_THRESHOLD = "ram_alert_threshold"
    private const val KEY_FIRST_RUN = "first_run"
    private const val KEY_SHIZUKU_CONNECTED = "shizuku_connected"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var autoOptimize: Boolean
        get() = TurboXApp.instance.let { prefs(it).getBoolean(KEY_AUTO_OPTIMIZE, false) }
        set(v) = prefs(TurboXApp.instance).edit().putBoolean(KEY_AUTO_OPTIMIZE, v).apply()

    var optimizeOnBoot: Boolean
        get() = prefs(TurboXApp.instance).getBoolean(KEY_OPTIMIZE_ON_BOOT, false)
        set(v) = prefs(TurboXApp.instance).edit().putBoolean(KEY_OPTIMIZE_ON_BOOT, v).apply()

    var animationScale: Float
        get() = prefs(TurboXApp.instance).getFloat(KEY_ANIMATION_SCALE, 1.0f)
        set(v) = prefs(TurboXApp.instance).edit().putFloat(KEY_ANIMATION_SCALE, v).apply()

    var performanceProfile: Int
        get() = prefs(TurboXApp.instance).getInt(KEY_PERFORMANCE_PROFILE, 1)
        set(v) = prefs(TurboXApp.instance).edit().putInt(KEY_PERFORMANCE_PROFILE, v).apply()

    var monitorEnabled: Boolean
        get() = prefs(TurboXApp.instance).getBoolean(KEY_MONITOR_ENABLED, true)
        set(v) = prefs(TurboXApp.instance).edit().putBoolean(KEY_MONITOR_ENABLED, v).apply()

    var tempAlertThreshold: Float
        get() = prefs(TurboXApp.instance).getFloat(KEY_TEMP_ALERT_THRESHOLD, 50f)
        set(v) = prefs(TurboXApp.instance).edit().putFloat(KEY_TEMP_ALERT_THRESHOLD, v).apply()

    var ramAlertThreshold: Float
        get() = prefs(TurboXApp.instance).getFloat(KEY_RAM_ALERT_THRESHOLD, 90f)
        set(v) = prefs(TurboXApp.instance).edit().putFloat(KEY_RAM_ALERT_THRESHOLD, v).apply()

    var isFirstRun: Boolean
        get() = prefs(TurboXApp.instance).getBoolean(KEY_FIRST_RUN, true)
        set(v) = prefs(TurboXApp.instance).edit().putBoolean(KEY_FIRST_RUN, v).apply()

    var shizukuEverConnected: Boolean
        get() = prefs(TurboXApp.instance).getBoolean(KEY_SHIZUKU_CONNECTED, false)
        set(v) = prefs(TurboXApp.instance).edit().putBoolean(KEY_SHIZUKU_CONNECTED, v).apply()
}
