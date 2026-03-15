/*
 * SPDX-FileCopyrightText: 2016 The CyanogenMod Project
 * SPDX-FileCopyrightText: 2018 The LineageOS Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat

/**
 * A SwitchPreference which can automatically remove itself from the hierarchy
 * based on constraints set in XML.
 */
abstract class SelfRemovingSwitchPreference : SwitchPreferenceCompat {

    private val mConstraints: ConstraintsHelper

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
        super(context, attrs, defStyle) {
        mConstraints = ConstraintsHelper(context, attrs, this)
        preferenceDataStore = DataStore()
    }

    constructor(context: Context, attrs: AttributeSet?) :
        super(context, attrs) {
        mConstraints = ConstraintsHelper(context, attrs, this)
        preferenceDataStore = DataStore()
    }

    constructor(context: Context) :
        super(context) {
        mConstraints = ConstraintsHelper(context, null, this)
        preferenceDataStore = DataStore()
    }

    override fun onAttached() {
        super.onAttached()
        mConstraints.onAttached()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        mConstraints.onBindViewHolder(holder)
    }

    fun setAvailable(available: Boolean) {
        mConstraints.setAvailable(available)
    }

    fun isAvailable(): Boolean {
        return mConstraints.isAvailable()
    }

    protected abstract fun isPersisted(): Boolean
    protected abstract fun putBoolean(key: String, value: Boolean)
    protected abstract fun getBoolean(key: String, defaultValue: Boolean): Boolean

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {

        val checked: Boolean

        if (!restorePersistedValue || !isPersisted()) {

            if (defaultValue == null) {
                return
            }

            checked = defaultValue as Boolean

            if (shouldPersist()) {
                persistBoolean(checked)
            }

        } else {
            // Note: the default is not used because to have got here
            // isPersisted() must be true.
            checked = getBoolean(key, false)
        }

        setChecked(checked)
    }

    private inner class DataStore : PreferenceDataStore() {

        override fun putBoolean(key: String, value: Boolean) {
            this@SelfRemovingSwitchPreference.putBoolean(key, value)
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return this@SelfRemovingSwitchPreference.getBoolean(key, defaultValue)
        }
    }
}
