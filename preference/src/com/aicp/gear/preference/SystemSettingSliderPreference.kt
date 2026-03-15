/*
 * SPDX-FileCopyrightText: 2016-2025 crDroid Android Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.preference

import android.content.Context
import android.util.AttributeSet

class SystemSettingSliderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseSliderPreference(context, attrs) {

    init {
        preferenceDataStore = SystemSettingsStore(context.contentResolver)
    }
}
