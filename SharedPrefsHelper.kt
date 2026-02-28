package com.example.privacyawareinterface.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsHelper {

    // ==============================
    // 📊 APP USAGE TRACKING
    // ==============================
    fun saveAppUsage(context: Context, packageName: String, timeMillis: Long) {
        val prefs = context.getSharedPreferences("app_usage", Context.MODE_PRIVATE)
        prefs.edit().putLong(packageName, timeMillis).apply()
    }

    fun getAppUsage(context: Context, packageName: String): Long {
        val prefs = context.getSharedPreferences("app_usage", Context.MODE_PRIVATE)
        return prefs.getLong(packageName, 0L)
    }

    // ==============================
    // 🔐 MAIN PRIVACY PREFS
    // ==============================
    private const val PREF_NAME = "privacy_aware_prefs"
    private const val KEY_ALLOWED_APPS = "allowed_apps"
    private const val KEY_BLOCKED_APPS = "blocked_apps_set"
    private const val KEY_PROTECTION_ENABLED = "protection_enabled"
    private const val KEY_CHILD_ID = "child_id" // optional if you want multi-child Firebase sync support

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ==============================
    // ✅ Allowed / Blocked Apps
    // ==============================
    /** ✅ Save allowed package list (for child’s dashboard) */
    fun saveAllowedApps(context: Context, allowedApps: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_ALLOWED_APPS, allowedApps).apply()
    }

    /** ✅ Get allowed package list */
    fun getAllowedApps(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_ALLOWED_APPS, emptySet()) ?: emptySet()
    }

    /** ✅ Save blocked package list (used by AppBlockerService & Firebase sync) */
    fun saveBlockedApps(context: Context, apps: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_BLOCKED_APPS, apps).apply()
    }

    /** ✅ Get blocked package list */
    fun getBlockedApps(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet()
    }

    /** ✅ Add a single blocked app */
    fun addBlockedApp(context: Context, packageName: String) {
        val set = getBlockedApps(context).toMutableSet()
        set.add(packageName)
        saveBlockedApps(context, set)
    }

    /** ✅ Remove a single blocked app */
    fun removeBlockedApp(context: Context, packageName: String) {
        val set = getBlockedApps(context).toMutableSet()
        set.remove(packageName)
        saveBlockedApps(context, set)
    }

    // ==============================
    // ⚙️ Protection System Toggle
    // ==============================
    /** ✅ Turn on/off always-on protection */
    fun setProtectionEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).apply()
    }

    /** ✅ Check if background monitoring is enabled */
    fun isProtectionEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PROTECTION_ENABLED, false)
    }

    // ==============================
    // 👶 Optional: Child Identification (for Firebase sync)
    // ==============================
    fun saveChildId(context: Context, childId: String) {
        getPrefs(context).edit().putString(KEY_CHILD_ID, childId).apply()
    }

    fun getChildId(context: Context): String? {
        return getPrefs(context).getString(KEY_CHILD_ID, null)
    }

    // ==============================
    // 🧹 Clear Everything (logout / reset)
    // ==============================
    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
