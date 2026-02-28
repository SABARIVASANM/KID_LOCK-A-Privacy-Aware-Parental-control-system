package com.example.privacyawareinterface.monitor

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * AccessibilityService that detects the current foreground window/package.
 * It forwards package names to AppBlockerService via an Intent action.
 */
class AppBlockerAccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "AppBlockerA11y"
        const val ACTION_FOREGROUND_PKG = "com.example.privacyawareinterface.monitor.ACTION_FOREGROUND_PKG"
        const val EXTRA_FOREGROUND_PKG = "extra_foreground_pkg"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        serviceInfo = info
        Log.d(TAG, "A11y service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event == null) return
            val pkg = event.packageName?.toString() ?: return

            // Ignore our own package to avoid self-loop
            if (pkg == applicationContext.packageName) return

            // forward to AppBlockerService
            val intent = Intent(this, AppBlockerService::class.java).apply {
                action = ACTION_FOREGROUND_PKG
                putExtra(EXTRA_FOREGROUND_PKG, pkg)
            }
            // safe: start service so service can react
            startService(intent)
        } catch (t: Throwable) {
            Log.e(TAG, "onAccessibilityEvent error", t)
        }
    }

    override fun onInterrupt() { /* no-op */ }
}
