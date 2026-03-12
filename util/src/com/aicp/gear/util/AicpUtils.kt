/*
 * SPDX-FileCopyrightText: 2017-2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Color
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.PowerManager
import android.os.SystemClock
import android.os.UserHandle
import android.util.Log
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import java.util.Locale

object AicpUtils {

    private const val TAG = "AicpUtils"

    fun isChineseLanguage(): Boolean {
        return Resources.getSystem().configuration.locale.language
            .startsWith(Locale.CHINESE.language)
    }

    @JvmStatic
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        return try {
            val packages = pm?.getInstalledApplications(0)
            packages?.any { it.packageName == packageName } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            false
        }
    }

    @JvmStatic
    fun isPackageEnabled(packageName: String, pm: PackageManager): Boolean {
        return try {
            val ai = pm.getApplicationInfo(packageName, 0)
            ai.enabled
        } catch (notFound: PackageManager.NameNotFoundException) {
            false
        }
    }

    @JvmStatic
    fun isPackageEnabled(packageName: String, context: Context): Boolean {
        return isPackageEnabled(packageName, context.packageManager)
    }

    @JvmStatic
    fun isPackageAvailable(packageName: String, context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            val enabled = pm.getApplicationEnabledSetting(packageName)
            enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isServiceRunning(context: Context, serviceName: String): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Int.MAX_VALUE)

        services?.forEach { info ->
            val className = info.service?.className
            if (className != null && className.equals(serviceName, true)) {
                return true
            }
        }
        return false
    }

    fun hasCamera(context: Context): Boolean {
        val pm = context.packageManager
        return pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    fun hasFrontCamera(context: Context): Boolean {
        val pm = context.packageManager
        return pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    fun deviceSupportsFlashLight(context: Context): Boolean {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            for (id in cameraManager.cameraIdList) {
                val c = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val lensFacing = c.get(CameraCharacteristics.LENS_FACING)
                if (flashAvailable == true &&
                    lensFacing == CameraCharacteristics.LENS_FACING_BACK
                ) {
                    return true
                }
            }
            false
        } catch (e: CameraAccessException) {
            false
        }
    }

    @JvmStatic
    fun resolveIntent(context: Context, intent: Intent): Boolean {
        val pm = context.packageManager
        val results: List<ResolveInfo> =
            pm.queryIntentActivitiesAsUser(
                intent,
                PackageManager.MATCH_SYSTEM_ONLY,
                UserHandle.myUserId()
            )

        for (resolveInfo in results) {
            if ((resolveInfo.activityInfo.applicationInfo.flags and
                        ApplicationInfo.FLAG_SYSTEM) != 0
            ) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun resolveIntent(context: Context, action: String): Boolean {
        return resolveIntent(context, Intent(action))
    }

    const val APP_PACKAGE_NAME = "org.omnirom.omniswitch"

    private const val ACTION_TOGGLE_OVERLAY2 =
        "$APP_PACKAGE_NAME.ACTION_TOGGLE_OVERLAY2"
    private const val ACTION_PRELOAD_TASKS =
        "$APP_PACKAGE_NAME.ACTION_PRELOAD_TASKS"
    private const val ACTION_RESTORE_HOME_STACK =
        "$APP_PACKAGE_NAME.ACTION_RESTORE_HOME_STACK"
    private const val ACTION_HIDE_OVERLAY =
        "$APP_PACKAGE_NAME.ACTION_HIDE_OVERLAY"

    val INTENT_LAUNCH_APP: Intent =
        Intent(Intent.ACTION_MAIN).setClassName(
            APP_PACKAGE_NAME,
            "$APP_PACKAGE_NAME.SettingsActivity"
        )

    fun isBillingBypassInstalled(context: Context): Boolean {
        val packages = arrayOf(
            "com.dimonvideo.luckypatcher",
            "com.chelpus.lackypatch",
            "com.android.vending.billing.InAppBillingService.LACK",
            "com.android.vending.billing.InAppBillingService.LOCK",
            "com.android.vending.billing.InAppBillingService.CLON",
            "com.android.vendinc",
            "uret.jasi2169.patcher"
        )

        return packages.any { isPackageInstalled(context, it) }
    }

    fun toggleOmniSwitchRecents(context: Context, user: UserHandle) {
        val intent = Intent(ACTION_TOGGLE_OVERLAY2)
        intent.setPackage(APP_PACKAGE_NAME)
        context.sendBroadcastAsUser(intent, user)
    }

    fun hideOmniSwitchRecents(context: Context, user: UserHandle) {
        val intent = Intent(ACTION_HIDE_OVERLAY)
        intent.setPackage(APP_PACKAGE_NAME)
        context.sendBroadcastAsUser(intent, user)
    }

    fun restoreHomeStack(context: Context, user: UserHandle) {
        val intent = Intent(ACTION_RESTORE_HOME_STACK)
        intent.setPackage(APP_PACKAGE_NAME)
        context.sendBroadcastAsUser(intent, user)
    }

    fun preloadOmniSwitchRecents(context: Context, user: UserHandle) {
        val intent = Intent(ACTION_PRELOAD_TASKS)
        intent.setPackage(APP_PACKAGE_NAME)
        context.sendBroadcastAsUser(intent, user)
    }

    @JvmStatic
    fun getBlendColorForPercent(
        fullColor: Int,
        emptyColor: Int,
        reversed: Boolean,
        percentage: Int
    ): Int {

        val newColor = FloatArray(3)
        val empty = FloatArray(3)
        val full = FloatArray(3)

        Color.colorToHSV(fullColor, full)
        val fullAlpha = Color.alpha(fullColor)

        Color.colorToHSV(emptyColor, empty)
        val emptyAlpha = Color.alpha(emptyColor)

        val blendFactor = percentage / 100f

        if (reversed) {
            if (empty[0] < full[0]) empty[0] += 360f
            newColor[0] = empty[0] - (empty[0] - full[0]) * blendFactor
        } else {
            if (empty[0] > full[0]) full[0] += 360f
            newColor[0] = empty[0] + (full[0] - empty[0]) * blendFactor
        }

        when {
            newColor[0] > 360f -> newColor[0] -= 360f
            newColor[0] < 0 -> newColor[0] += 360f
        }

        newColor[1] = empty[1] + (full[1] - empty[1]) * blendFactor
        newColor[2] = empty[2] + (full[2] - empty[2]) * blendFactor

        val newAlpha = (emptyAlpha + (fullAlpha - emptyAlpha) * blendFactor).toInt()

        return Color.HSVToColor(newAlpha, newColor)
    }

    fun switchScreenOff(ctx: Context) {
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.goToSleep(SystemClock.uptimeMillis())
    }

    fun sendKeycode(keycode: Int) {
        val whenTime = SystemClock.uptimeMillis()

        val evDown = KeyEvent(
            whenTime,
            whenTime,
            KeyEvent.ACTION_DOWN,
            keycode,
            0,
            0,
            KeyCharacterMap.VIRTUAL_KEYBOARD,
            0,
            KeyEvent.FLAG_FROM_SYSTEM or KeyEvent.FLAG_VIRTUAL_HARD_KEY,
            InputDevice.SOURCE_KEYBOARD
        )

        val evUp = KeyEvent.changeAction(evDown, KeyEvent.ACTION_UP)
    }

    fun getRunningActivityInfo(context: Context): ActivityInfo? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = context.packageManager

        val tasks = am.getRunningTasks(1)
        if (!tasks.isNullOrEmpty()) {
            val top = tasks[0]
            val component = top.topActivity ?: return null

            return try {
                pm.getActivityInfo(component, 0)
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
        return null
    }
}
