/*
 * SPDX-FileCopyrightText: 2014 SlimRoms Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.util

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import java.io.File
import java.net.URISyntaxException
import java.util.ArrayList

class ActionHelper {

    companion object {

        private const val SYSTEMUI_METADATA_NAME = "com.android.systemui"

        @JvmStatic
        fun getRecentAppSidebarConfig(context: Context): ArrayList<ActionConfig> {
            return ConfigSplitHelper.getActionConfigValues(
                context,
                getRecentAppSidebarProvider(context),
                null,
                null,
                false
            )
        }

        fun getRecentAppSidebarConfigWithDescription(
            context: Context,
            values: String?,
            entries: String?
        ): ArrayList<ActionConfig> {
            return ConfigSplitHelper.getActionConfigValues(
                context,
                getRecentAppSidebarProvider(context),
                values,
                entries,
                false
            )
        }

        private fun getRecentAppSidebarProvider(context: Context): String {
            var config = Settings.System.getStringForUser(
                context.contentResolver,
                Settings.System.RECENT_APP_SIDEBAR_CONTENT,
                UserHandle.USER_CURRENT
            )
            if (config == null) {
                config = ""
            }
            return config
        }

        fun setRecentAppSidebarConfig(
            context: Context,
            actionConfig: ArrayList<ActionConfig>,
            reset: Boolean
        ) {
            val config = if (reset) {
                ""
            } else {
                ConfigSplitHelper.setActionConfig(actionConfig, false)
            }

            Settings.System.putString(
                context.contentResolver,
                Settings.System.RECENT_APP_SIDEBAR_CONTENT,
                config
            )
        }

        @JvmStatic
        fun getActionIconImage(
            context: Context,
            clickAction: String,
            customIcon: String?
        ): Drawable? {
            return getActionIconImage(context, clickAction, customIcon, null)
        }

        @JvmStatic
        fun getActionIconImage(
            context: Context,
            clickAction: String,
            customIcon: String?,
            iconsHandler: AbstractIconsHandler?
        ): Drawable? {

            var resId = -1
            var d: Drawable? = null

            val pm = context.packageManager ?: return null

            val systemUiResources: Resources = try {
                pm.getResourcesForApplication(SYSTEMUI_METADATA_NAME)
            } catch (e: Exception) {
                Log.e("ButtonsHelper:", "can't access systemui resources", e)
                return null
            }

            if (!clickAction.startsWith("**")) {
                try {
                    val extraIconPath =
                        clickAction.replace(Regex(".*?hasExtraIcon="), "")

                    if (!extraIconPath.isNullOrEmpty()) {
                        val f = File(Uri.parse(extraIconPath).path!!)
                        if (f.exists()) {
                            d = BitmapDrawable(
                                context.resources,
                                f.absolutePath
                            )
                        }
                    }

                    if (d == null) {
                        if (iconsHandler != null) {
                            try {
                                val info: ActivityInfo = pm.getActivityInfo(
                                    Intent.parseUri(clickAction, 0).component!!,
                                    0
                                )
                                return iconsHandler.getIconFromHandler(context, info)
                            } catch (_: PackageManager.NameNotFoundException) {
                            }
                        }

                        d = pm.getActivityIcon(Intent.parseUri(clickAction, 0))
                    }

                } catch (e: PackageManager.NameNotFoundException) {

                    resId = systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_null",
                        null,
                        null
                    )

                    if (resId > 0) {
                        d = systemUiResources.getDrawable(resId)
                        return d
                    }

                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }
            }

            if (customIcon != null &&
                customIcon.startsWith(ActionConstants.SYSTEM_ICON_IDENTIFIER)
            ) {

                resId = systemUiResources.getIdentifier(
                    customIcon.substring(ActionConstants.SYSTEM_ICON_IDENTIFIER.length),
                    "drawable",
                    "android"
                )

                if (resId > 0) {
                    return systemUiResources.getDrawable(resId)
                }

            } else if (customIcon != null &&
                customIcon != ActionConstants.ICON_EMPTY
            ) {

                val f = File(Uri.parse(customIcon).path!!)
                if (f.exists()) {
                    return BitmapDrawable(
                        context.resources,
                        ImageHelper.getRoundedCornerBitmap(
                            BitmapDrawable(context.resources, f.absolutePath).bitmap
                        )
                    )
                } else {
                    Log.e("ActionHelper:", "can't access custom icon image")
                    return null
                }

            } else if (clickAction.startsWith("**")) {

                resId = getActionSystemIcon(systemUiResources, clickAction)

                if (resId > 0) {
                    return systemUiResources.getDrawable(resId)
                }
            }

            return d
        }

        private fun getActionSystemIcon(
            systemUiResources: Resources,
            clickAction: String
        ): Int {

            return when (clickAction) {

                ActionConstants.ACTION_HOME ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_home",
                        null,
                        null
                    )

                ActionConstants.ACTION_BACK ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_back",
                        null,
                        null
                    )

                ActionConstants.ACTION_RECENTS ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_recent",
                        null,
                        null
                    )

                ActionConstants.ACTION_SEARCH,
                ActionConstants.ACTION_ASSIST ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_search",
                        null,
                        null
                    )

                ActionConstants.ACTION_KEYGUARD_SEARCH ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_search_light",
                        null,
                        null
                    )

                ActionConstants.ACTION_MENU ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_menu",
                        null,
                        null
                    )

                ActionConstants.ACTION_MENU_BIG ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_menu_big",
                        null,
                        null
                    )

                ActionConstants.ACTION_IME ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_ime_switcher",
                        null,
                        null
                    )

                ActionConstants.ACTION_KILL ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_killtask",
                        null,
                        null
                    )

                ActionConstants.ACTION_POWER ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_power",
                        null,
                        null
                    )

                ActionConstants.ACTION_POWER_MENU ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_power_menu",
                        null,
                        null
                    )

                ActionConstants.ACTION_VIB ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_vib",
                        null,
                        null
                    )

                ActionConstants.ACTION_SILENT ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_silent",
                        null,
                        null
                    )

                ActionConstants.ACTION_VIB_SILENT ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_ring_vib_silent",
                        null,
                        null
                    )

                else ->
                    systemUiResources.getIdentifier(
                        "$SYSTEMUI_METADATA_NAME:drawable/ic_sysbar_null",
                        null,
                        null
                    )
            }
        }
    }
}
