/*
 * SPDX-FileCopyrightText: 2014 The NamelessROM Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent

object OnTheGoActions {

    const val ACTION_ONTHEGO_TOGGLE = "action_onthego_toggle"

    @JvmStatic
    fun processAction(context: Context, action: String?) {

        if (action == null || action.isEmpty()) {
            return
        }

        if (ACTION_ONTHEGO_TOGGLE == action) {
            actionOnTheGoToggle(context)
        }
    }

    private fun actionOnTheGoToggle(context: Context) {
        val cn = ComponentName(
            "com.android.systemui",
            "com.android.systemui.aicp.onthego.OnTheGoService"
        )

        val startIntent = Intent()
        startIntent.component = cn
        startIntent.action = "start"

        context.startService(startIntent)
    }
}
