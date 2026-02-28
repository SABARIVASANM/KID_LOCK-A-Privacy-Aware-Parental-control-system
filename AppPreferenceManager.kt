package com.example.privacyawareinterface.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.edit

/**
 * Centralized shared-preference helper.
 * - blocked apps set
 * - per-package usage totals (ms)
 * - per-package running start timestamp (ms)
 * - remembers last launched package so ChildDashboard can stop onResume
 */
object AppPreferenceManager {
    private const val PREF_NAME = "privacy_pref_v2"
    private const val KEY_BLOCKED = "blocked_apps_v2"              // StringSet
    private const val KEY_USAGE_PREFIX = "usage_total_ms_v2_"      // + packageName -> Long (ms)
    private const val KEY_START_PREFIX = "usage_start_ts_v2_"      // + packageName -> Long (ms)
    private const val KEY_LAST_LAUNCHED = "last_launched_pkg_v2"  // last launched package (String)

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ---------- Block / Allow ----------
    fun blockApp(context: Context, packageName: String) {
        val set = prefs(context).getStringSet(KEY_BLOCKED,
            mutableSetOf()
        )?.toMutableSet() ?: mutableSetOf()
        if (!set.contains(packageName)) {
            set.add(packageName)
            prefs(context).edit { putStringSet(KEY_BLOCKED, set) }
        }
    }

    fun allowApp(context: Context, packageName: String) {
        val set = prefs(context).getStringSet(KEY_BLOCKED,
            mutableSetOf()
        )?.toMutableSet() ?: mutableSetOf()
        if (set.remove(packageName)) {
            prefs(context).edit { putStringSet(KEY_BLOCKED, set) }
        }
    }

    fun getBlockedApps(context: Context): MutableSet<String> {
        return prefs(context).getStringSet(KEY_BLOCKED,
            mutableSetOf()
        )?.toMutableSet() ?: mutableSetOf()
    }

    fun isAppBlocked(context: Context, packageName: String): Boolean {
        return getBlockedApps(context).contains(packageName)
    }

    // ---------- Usage tracking (ms) ----------
    fun startAppTimer(context: Context, packageName: String) {
        val now = System.currentTimeMillis()
        prefs(context).edit {
            putLong(KEY_START_PREFIX + packageName, now)
            putString(KEY_LAST_LAUNCHED, packageName)
        }
    }

    fun stopAppTimer(context: Context, packageName: String) {
        val p = prefs(context)
        val startKey = KEY_START_PREFIX + packageName
        if (!p.contains(startKey)) return // nothing to add

        val startTs = p.getLong(startKey, 0L)
        if (startTs <= 0L) {
            p.edit { remove(startKey) }
            return
        }

        val now = System.currentTimeMillis()
        val delta = now - startTs
        if (delta > 0L) {
            val usageKey = KEY_USAGE_PREFIX + packageName
            val prev = p.getLong(usageKey, 0L)
            p.edit {
                putLong(usageKey, prev + delta)
                remove(startKey)
            }
        } else {
            p.edit { remove(startKey) } // guard
        }
    }

    fun addUsageMillis(context: Context, packageName: String, extraMs: Long) {
        if (extraMs <= 0L) return
        val usageKey = KEY_USAGE_PREFIX + packageName
        val prev = prefs(context).getLong(usageKey, 0L)
        prefs(context).edit { putLong(usageKey, prev + extraMs) }
    }

    fun getUsageMillis(context: Context, packageName: String): Long {
        return prefs(context).getLong(KEY_USAGE_PREFIX + packageName, 0L)
    }

    fun getUsageMinutes(context: Context, packageName: String): Long {
        return getUsageMillis(context, packageName) / 1000L / 60L
    }

    // ---------- Last launched helpers ----------
    fun setLastLaunched(context: Context, packageName: String) {
        prefs(context).edit { putString(KEY_LAST_LAUNCHED, packageName) }
    }

    fun getLastLaunched(context: Context): String? {
        return prefs(context).getString(KEY_LAST_LAUNCHED, null)
    }

    fun clearLastLaunched(context: Context) {
        prefs(context).edit { remove(KEY_LAST_LAUNCHED) }
    }

    // ---------- Allowed apps helpers ----------
    data class AppDetail(val appName: String, val packageName: String, val icon: Drawable?)

    fun getAllowedPackageNames(context: Context): List<String> {
        val pm = context.packageManager
        val blocked = getBlockedApps(context)
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null } // launchable
            .map { it.packageName }
            .filter { !blocked.contains(it) }
            .sorted()
    }

    fun getAllowedAppDetails(context: Context): List<AppDetail> {
        val pm = context.packageManager
        val allowedPkgs = getAllowedPackageNames(context)
        return allowedPkgs.mapNotNull { pkg ->
            try {
                val ai = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(ai).toString()
                val icon = pm.getApplicationIcon(ai)
                AppDetail(label, pkg, icon)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.appName.lowercase() }
    }

    fun getAllUserAppDetails(context: Context): List<AppDetail> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 } // user apps
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null } // launchable
            .mapNotNull {
                try {
                    val label = pm.getApplicationLabel(it).toString()
                    val icon = pm.getApplicationIcon(it.packageName)
                    AppDetail(label, it.packageName, icon)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.appName.lowercase() }
    }

    // ---------- Utility (clear) ----------
    fun clearAll(context: Context) {
        prefs(context).edit { clear() }
    }
}
