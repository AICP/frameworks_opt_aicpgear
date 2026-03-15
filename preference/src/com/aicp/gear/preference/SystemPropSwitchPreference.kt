/*
 * SPDX-FileCopyrightText: 2020-2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat

class SystemPropSwitchPreference : SwitchPreferenceCompat {

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
        super(context, attrs, defStyle) {
        preferenceDataStore = SystemPropStore()
    }

    constructor(context: Context, attrs: AttributeSet?) :
        super(context, attrs) {
        preferenceDataStore = SystemPropStore()
    }

    constructor(context: Context) :
        super(context) {
        preferenceDataStore = SystemPropStore()
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        // This is what default TwoStatePreference implementation is doing without respecting
        // real default value:
        // setChecked(restoreValue ? getPersistedBoolean(mChecked)
        //        : (Boolean) defaultValue);
        // Instead, we better do
        setChecked(
            if (restoreValue)
                getPersistedBoolean(defaultValue as Boolean)
            else
                defaultValue as Boolean
        )
    }
}
