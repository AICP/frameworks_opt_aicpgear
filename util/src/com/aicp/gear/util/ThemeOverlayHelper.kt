/*
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.util

import android.app.ActivityManager
import android.content.Context
import android.content.om.IOverlayManager
import android.content.om.OverlayInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.RemoteException
import android.os.ServiceManager
import android.os.UserHandle
import android.text.TextUtils
import android.util.Log
import java.util.*

class ThemeOverlayHelper(
    private val context: Context,
    private val category: String,
    target: String
) {
    companion object {
        private const val TAG = "ThemeOverlayHelper"
        const val NOVERLAY_PKG = "default_pkg"

        private val ICON_PACK_CATEGORIES = arrayOf(
            "android.theme.customization.icon_pack.android",
            "android.theme.customization.icon_pack.systemui",
            "android.theme.customization.icon_pack.settings",
            "android.theme.customization.icon_pack.launcher",
            "android.theme.customization.icon_pack.themepicker"
        )

        @JvmStatic
        fun setCutoutOverlay(om: android.content.om.OverlayManager, enable: Boolean) {
            val userId = UserHandle.of(ActivityManager.getCurrentUser())
            try {
                om.setEnabled("com.android.overlay.hidecutout", enable, userId)
            } catch (e: Exception) {
                // Ignore
            }
        }

        @JvmStatic
        fun setStatusBarStockOverlay(om: android.content.om.OverlayManager, enable: Boolean) {
            val userId = UserHandle.of(ActivityManager.getCurrentUser())
            try {
                om.setEnabled("com.android.overlay.statusbarstock", enable, userId)
                om.setEnabled("com.android.overlay.statusbarstocksysui", enable, userId)
            } catch (e: Exception) {
                // Ignore
            }
        }

        @JvmStatic
        fun setImmersiveOverlay(om: android.content.om.OverlayManager, enable: Boolean) {
            val userId = UserHandle.of(ActivityManager.getCurrentUser())
            try {
                om.setEnabled("com.android.overlay.immersive", enable, userId)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private val overlayService: IOverlayManager =
        IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
    private val packageManager: PackageManager = context.packageManager
    private val targets: Array<String> = target.split(";").toTypedArray()

    private fun isTheme(oi: OverlayInfo): Boolean {
        if (category != oi.category) {
            return false
        }
        return try {
            val pi = packageManager.getPackageInfo(oi.packageName, 0)
            pi != null && !pi.isStaticOverlayPackage()
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getCurrentTheme(): String {
        val themePackages = getAvailableThemes(currentThemeOnly = true, target = targets[0])
        return if (themePackages.isEmpty()) NOVERLAY_PKG else themePackages[0]
    }

    fun getAvailableThemes(): Array<String> {
        return getAvailableThemes(currentThemeOnly = false, target = targets[0])
    }

    private fun getAvailableThemes(currentThemeOnly: Boolean, target: String): Array<String> {
        val infos: List<OverlayInfo>
        val pkgs: MutableList<String> = ArrayList()
        try {
            infos = overlayService.getOverlayInfosForTarget(target, UserHandle.myUserId())
            for (i in infos.indices) {
                if (isTheme(infos[i])) {
                    if (infos[i].isEnabled && currentThemeOnly) {
                        return arrayOf(infos[i].packageName)
                    } else {
                        pkgs.add(infos[i].packageName)
                    }
                }
            }
        } catch (re: RemoteException) {
            throw re.rethrowFromSystemServer()
        }

        if (currentThemeOnly) {
            return emptyArray()
        }
        Collections.sort(pkgs)
        pkgs.add(0, NOVERLAY_PKG)
        return pkgs.toTypedArray()
    }

    fun setTheme(value: String): Boolean {
        val current = getCurrentTheme()
        if (Objects.equals(value, current)) {
            return true
        }
        if (NOVERLAY_PKG == value && TextUtils.isEmpty(current) || TextUtils.equals(value, current)) {
            return true
        }
        if (ICON_PACK_CATEGORIES[0] == category) {
            if (NOVERLAY_PKG == value) {
                val basePkg = current.substring(0, current.lastIndexOf("."))
                for (cat in ICON_PACK_CATEGORIES) {
                    val pkg = basePkg + cat.substring(cat.lastIndexOf("."))
                    try {
                        overlayService.setEnabled(pkg, false, UserHandle.myUserId())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                val basePkg = value.substring(0, value.lastIndexOf("."))
                for (cat in ICON_PACK_CATEGORIES) {
                    val pkg = basePkg + cat.substring(cat.lastIndexOf("."))
                    try {
                        overlayService.setEnabledExclusiveInCategory(pkg, UserHandle.myUserId())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return true
        }
        if (NOVERLAY_PKG == value) {
            try {
                overlayService.setEnabled(current, false, UserHandle.myUserId())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            for (i in 1 until targets.size) {
                val pkg = current.replace(targets[0], targets[i])
                try {
                    overlayService.setEnabled(pkg, false, UserHandle.myUserId())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            try {
                overlayService.setEnabledExclusiveInCategory(value, UserHandle.myUserId())
            } catch (re: RemoteException) {
                throw re.rethrowFromSystemServer()
            }
            for (i in 1 until targets.size) {
                val pkg = value.replace(targets[0], targets[i])
                try {
                    overlayService.setEnabled(pkg, true, UserHandle.myUserId())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (NOVERLAY_PKG != current) {
                    val prevPkg = current.replace(targets[0], targets[i])
                    try {
                        overlayService.setEnabled(prevPkg, false, UserHandle.myUserId())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return true
    }

    fun getThemeLabel(value: String): CharSequence {
        if (NOVERLAY_PKG == value) {
            return context.getString(R.string.theme_noverlay)
        }
        return try {
            packageManager.getApplicationInfo(value, 0).loadLabel(packageManager)
        } catch (e: PackageManager.NameNotFoundException) {
            value
        }
    }

    fun getThemeColor(value: String, resName: String): Int {
        if (NOVERLAY_PKG == value) {
            return 0
        }
        return try {
            val res = packageManager.getResourcesForApplication(value)
            val id = res.getIdentifier(resName, "color", value)
            res.getColor(id, null)
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    fun getThemeLabels(vararg pkgs: String): Array<CharSequence> {
        return Array(pkgs.size) { i -> getThemeLabel(pkgs[i]) }
    }

    fun getThemeColors(resName: String, vararg pkgs: String): IntArray {
        return IntArray(pkgs.size) { i -> getThemeColor(pkgs[i], resName) }
    }
}

