package com.example.tools

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.util.Locale

data class AppLaunchResult(
    val success: Boolean,
    val appName: String,
    val message: String
)

data class InstalledAppItem(
    val label: String,
    val packageName: String
)

object AppLauncherHelper {

    fun tryLaunchApp(context: Context, appQuery: String, userTitle: String = "Sir"): AppLaunchResult {
        val cleanQuery = appQuery.trim().lowercase(Locale.ROOT)
            .removePrefix("open ")
            .removePrefix("launch ")
            .removePrefix("start ")
            .removePrefix("run ")
            .removePrefix("app ")
            .removeSuffix(" app")
            .removeSuffix(" please")
            .trim()

        val pm = context.packageManager

        // 1. Direct standard intents for common built-in phone utilities
        when (cleanQuery) {
            "camera" -> {
                return try {
                    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    AppLaunchResult(true, "Camera", "Opening Camera for you, $userTitle.")
                } catch (e: Exception) {
                    launchByPackage(context, pm, "com.android.camera", "Camera", userTitle)
                }
            }
            "settings", "setting" -> {
                return try {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    AppLaunchResult(true, "Settings", "Opening System Settings, $userTitle.")
                } catch (e: Exception) {
                    AppLaunchResult(false, "Settings", "Unable to launch Settings: ${e.message}")
                }
            }
            "alarm", "clock" -> {
                return try {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    AppLaunchResult(true, "Clock", "Opening Clock & Alarms, $userTitle.")
                } catch (e: Exception) {
                    launchByPackage(context, pm, "com.google.android.deskclock", "Clock", userTitle)
                }
            }
            "browser", "internet" -> {
                return try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    AppLaunchResult(true, "Browser", "Opening Browser, $userTitle.")
                } catch (e: Exception) {
                    launchByPackage(context, pm, "com.android.chrome", "Chrome", userTitle)
                }
            }
        }

        // 2. Known common package lookup
        val commonPackageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "chrome" to "com.android.chrome",
            "spotify" to "com.spotify.music",
            "calculator" to "com.google.android.calculator",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "play store" to "com.android.vending",
            "playstore" to "com.android.vending",
            "telegram" to "org.telegram.messenger",
            "instagram" to "com.instagram.android",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android",
            "facebook" to "com.facebook.katana",
            "netflix" to "com.netflix.mediaclient",
            "photos" to "com.google.android.apps.photos",
            "drive" to "com.google.android.apps.docs",
            "contacts" to "com.google.android.contacts",
            "calendar" to "com.google.android.calendar"
        )

        if (commonPackageMap.containsKey(cleanQuery)) {
            val pkg = commonPackageMap[cleanQuery]!!
            val result = launchByPackage(context, pm, pkg, cleanQuery.replaceFirstChar { it.uppercase() }, userTitle)
            if (result.success) return result
        }

        // 3. Scan installed apps dynamically for matching display labels
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (info in resolveInfos) {
                val label = info.loadLabel(pm).toString()
                if (label.lowercase(Locale.ROOT).contains(cleanQuery) ||
                    cleanQuery.contains(label.lowercase(Locale.ROOT))
                ) {
                    val launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return AppLaunchResult(
                            success = true,
                            appName = label,
                            message = "Opening $label for you, $userTitle."
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // fallback
        }

        return AppLaunchResult(
            success = false,
            appName = cleanQuery,
            message = "Pardon me, $userTitle. I could not locate '$cleanQuery' installed on this device."
        )
    }

    private fun launchByPackage(
        context: Context,
        pm: PackageManager,
        packageName: String,
        displayName: String,
        userTitle: String
    ): AppLaunchResult {
        return try {
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                AppLaunchResult(true, displayName, "Launching $displayName for you, $userTitle.")
            } else {
                AppLaunchResult(false, displayName, "App $displayName is not installed on this device.")
            }
        } catch (e: Exception) {
            AppLaunchResult(false, displayName, "Unable to launch $displayName: ${e.message}")
        }
    }

    fun getInstalledLaunchableApps(context: Context): List<InstalledAppItem> {
        val pm = context.packageManager
        val list = mutableListOf<InstalledAppItem>()
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (info in resolveInfos) {
                val label = info.loadLabel(pm).toString()
                val pkg = info.activityInfo.packageName
                if (pkg != context.packageName) {
                    list.add(InstalledAppItem(label, pkg))
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return list.sortedBy { it.label.lowercase(Locale.ROOT) }
    }
}
