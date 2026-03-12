/*
 * SPDX-FileCopyrightText: 2014 SlimRoms Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

import android.content.Context

object Converter {

    @JvmStatic
    fun dpToPx(context: Context, dp: Int): Int {
        return ((dp * context.resources.displayMetrics.density) + 0.5f).toInt()
    }

    @JvmStatic
    fun pxToDp(context: Context, px: Int): Int {
        return ((px / context.resources.displayMetrics.density) + 0.5f).toInt()
    }
}
