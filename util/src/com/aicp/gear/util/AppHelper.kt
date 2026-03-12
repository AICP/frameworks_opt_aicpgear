/*
 * SPDX-FileCopyrightText: 2013 SlimRoms Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.Log
import java.net.URISyntaxException

object AppHelper {

    private const val SETTINGS_METADATA_NAME = "com.android.settings"

    @JvmStatic
    fun getProperSummary(
        context: Context,
        pm: PackageManager?,
        settingsResources: Resources?,
        action: String?,
        values: String?,
        entries: String?
    ): String {

        if (pm == null || settingsResources == null || action == null) {
            return context.resources.getString(
                com.android.internal.R.string.error_message_title
            )
        }

        if (values != null && entries != null) {
            val resIdEntries = settingsResources.getIdentifier(
                "$SETTINGS_METADATA_NAME:array/$entries", null, null
            )
            val resIdValues = settingsResources.getIdentifier(
                "$SETTINGS_METADATA_NAME:array/$values", null, null
            )

            if (resIdEntries > 0 && resIdValues > 0) {
                try {
                    val entriesArray = settingsResources.getStringArray(resIdEntries)
                    val valuesArray = settingsResources.getStringArray(resIdValues)
                    for (i in valuesArray.indices) {
                        if (action == valuesArray[i]) {
                            return entriesArray[i]
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return getFriendlyNameForUri(context, pm, action)
            ?: context.resources.getString(com.android.internal.R.string.error_message_title)
    }

    @JvmStatic
    fun getFriendlyActivityName(
        context: Context,
        pm: PackageManager,
        intent: Intent,
        labelOnly: Boolean
    ): String {
        val ai: ActivityInfo? = intent.resolveActivityInfo(pm, PackageManager.GET_ACTIVITIES)
        var friendlyName: String? = null

        if (ai != null) {
            friendlyName = ai.loadLabel(pm)?.toString()
            if (friendlyName == null && !labelOnly) {
                friendlyName = ai.name
            }
        }

        if (friendlyName == null || friendlyName.startsWith("#Intent;")) {
            return context.resources.getString(
                com.android.internal.R.string.error_message_title
            )
        }
        return if (friendlyName != null || labelOnly) friendlyName else intent.toUri(0)
    }

    @JvmStatic
    fun getFriendlyShortcutName(
        context: Context,
        pm: PackageManager,
        intent: Intent
    ): String {
        val activityName = getFriendlyActivityName(context, pm, intent, true)
        val name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

        if (activityName.startsWith("#Intent;")) {
            return context.resources.getString(
                com.android.internal.R.string.error_message_title
            )
        }

        return if (activityName != null && name != null) {
            "$activityName: $name"
        } else {
            name ?: intent.toUri(0)
        }
    }

    @JvmStatic
    fun getFriendlyNameForUri(
        context: Context,
        pm: PackageManager,
        uri: String?
    ): String? {
        if (uri == null || uri.startsWith("**")) {
            return null
        }

        return try {
            val intent = Intent.parseUri(uri, 0)
            if (Intent.ACTION_MAIN == intent.action) {
                getFriendlyActivityName(context, pm, intent, false)
            } else {
                getFriendlyShortcutName(context, pm, intent)
            }
        } catch (e: URISyntaxException) {
            uri
        }
    }

    @JvmStatic
    fun getShortcutPreferred(
        context: Context,
        pm: PackageManager,
        uri: String?
    ): String? {
        if (uri == null || uri.startsWith("**")) {
            return null
        }

        return try {
            val intent = Intent.parseUri(uri, 0)
            val name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
            if (name == null || name.startsWith("#Intent;")) {
                getFriendlyActivityName(context, pm, intent, false)
            } else {
                name
            }
        } catch (e: URISyntaxException) {
            uri
        }
    }
}
