/*
 * SPDX-FileCopyrightText: 2014 The NamelessROM Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log

object OnTheGoUtils {

    private const val TAG = "OnTheGoUtils"

    /**
     * Checks if a specific package is installed.
     *
     * @param context     The context to retrieve the package manager
     * @param packageName The name of the package
     * @return Whether the package is installed or not.
     */
    @JvmStatic
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        try {
            if (pm != null) {
                val packages: List<ApplicationInfo> = pm.getInstalledApplications(0)
                for (packageInfo in packages) {
                    if (packageInfo.packageName == packageName) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: " + e.message)
        }
        return false
    }

    /**
     * Checks if a specific service is running.
     *
     * @param context     The context to retrieve the activity manager
     * @param serviceName The name of the service
     * @return Whether the service is running or not
     */
    @JvmStatic
    fun isServiceRunning(context: Context, serviceName: String): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val services = activityManager.getRunningServices(Int.MAX_VALUE)

        if (services != null) {
            for (info in services) {
                val service = info.service
                if (service != null) {
                    val className = service.className
                    if (className != null && className.equals(serviceName, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * Check if system has a camera.
     */
    @JvmStatic
    fun hasCamera(context: Context): Boolean {
        val pm = context.packageManager
        return pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    /**
     * Check if system has a front camera.
     */
    @JvmStatic
    fun hasFrontCamera(context: Context): Boolean {
        val pm = context.packageManager
        return pm != null && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }
}
