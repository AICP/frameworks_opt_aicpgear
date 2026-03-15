/*
 * SPDX-FileCopyrightText: 2017-2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat

/**
 * Base class to simplify SwitchPreferences that store values in a custom ContentResolver-based store.
 */
abstract class BaseSettingSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = androidx.preference.R.attr.switchPreferenceCompatStyle
) : SwitchPreferenceCompat(context, attrs, defStyle) {

    init {
        preferenceDataStore = createDataStore(context)
    }

    /**
     * Subclasses provide the correct SettingsStore implementation
     */
    protected abstract fun createDataStore(context: Context): androidx.preference.PreferenceDataStore

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        isChecked = if (restoreValue) {
            getPersistedBoolean(isChecked)
        } else {
            defaultValue as? Boolean ?: false
        }
    }
}

/** Secure Settings Switch */
class SecureSettingSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = androidx.preference.R.attr.switchPreferenceCompatStyle
) : BaseSettingSwitchPreference(context, attrs, defStyle) {

    override fun createDataStore(context: Context) = SecureSettingsStore(context.contentResolver)
}

/** System Settings Switch */
class SystemSettingSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = androidx.preference.R.attr.switchPreferenceCompatStyle
) : BaseSettingSwitchPreference(context, attrs, defStyle) {

    override fun createDataStore(context: Context) = SystemSettingsStore(context.contentResolver)
}

/** Global Settings Switch */
class GlobalSettingSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = androidx.preference.R.attr.switchPreferenceCompatStyle
) : BaseSettingSwitchPreference(context, attrs, defStyle) {

    override fun createDataStore(context: Context) = GlobalSettingsStore(context.contentResolver)
}
