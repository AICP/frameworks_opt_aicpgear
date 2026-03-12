/*
 * SPDX-FileCopyrightText: 2014 SlimRoms Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.Log
import java.util.ArrayList

class ConfigSplitHelper {

    companion object {

        private const val SETTINGS_METADATA_NAME = "com.android.settings"

        @JvmStatic
        fun getActionConfigValues(
            context: Context,
            config: String,
            values: String?,
            entries: String?,
            isShortcut: Boolean
        ): ArrayList<ActionConfig> {

            var counter = 0
            val actionConfigList = ArrayList<ActionConfig>()
            var actionConfig: ActionConfig? = null

            val pm: PackageManager = context.packageManager
            var settingsResources: Resources? = null

            try {
                settingsResources = pm.getResourcesForApplication(SETTINGS_METADATA_NAME)
            } catch (e: Exception) {
                Log.e("ConfigSplitHelper", "can't access settings resources", e)
            }

            for (configValue in config.split("\\" + ActionConstants.ACTION_DELIMITER)) {
                counter++

                if (counter == 1) {
                    actionConfig = ActionConfig(
                        configValue,
                        AppHelper.getProperSummary(
                            context,
                            pm,
                            settingsResources,
                            configValue,
                            values,
                            entries
                        ),
                        null,
                        null,
                        null
                    )
                }

                if (counter == 2) {
                    if (isShortcut) {
                        actionConfig?.icon = configValue
                        actionConfigList.add(actionConfig!!)
                        counter = 0
                    } else {
                        actionConfig?.longpressAction = configValue
                        actionConfig?.longpressActionDescription =
                            AppHelper.getProperSummary(
                                context,
                                pm,
                                settingsResources,
                                configValue,
                                values,
                                entries
                            )
                    }
                }

                if (counter == 3) {
                    actionConfig?.icon = configValue
                    actionConfigList.add(actionConfig!!)
                    counter = 0
                }
            }

            return actionConfigList
        }

        @JvmStatic
        fun setActionConfig(
            actionConfigs: ArrayList<ActionConfig>,
            isShortcut: Boolean
        ): String {

            var finalConfig = ""
            lateinit var actionConfig: ActionConfig

            for (i in actionConfigs.indices) {

                if (i != 0) {
                    finalConfig += ActionConstants.ACTION_DELIMITER
                }

                actionConfig = actionConfigs[i]

                finalConfig += actionConfig.clickAction + ActionConstants.ACTION_DELIMITER

                if (!isShortcut) {
                    finalConfig += actionConfig.longpressAction +
                            ActionConstants.ACTION_DELIMITER
                }

                finalConfig += actionConfig.icon
            }

            return finalConfig
        }
    }
}
