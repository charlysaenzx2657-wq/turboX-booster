package com.optimizer.pro

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class OptimizerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Usado para funciones de UI automation si se necesita
    }

    override fun onInterrupt() {}
}
