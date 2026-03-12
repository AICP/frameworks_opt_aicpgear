/*
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable

interface AbstractIconsHandler {
    fun getIconFromHandler(context: Context, info: ActivityInfo): Drawable
}
