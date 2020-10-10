/*
 * Copyright (C) 2016 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aicp.gear.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.R;

/**
 * A PreferenceCategory which can automatically remove itself from the hierarchy
 * based on constraints set in XML.
 */
public class SelfRemovingPreferenceCategory extends PreferenceCategory {

    private final ConstraintsHelper mConstraints;

    public SelfRemovingPreferenceCategory(Context context, AttributeSet attrs,
                                  int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        mConstraints = new ConstraintsHelper(context, attrs, this);
    }

    public SelfRemovingPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public SelfRemovingPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, ConstraintsHelper.getAttr(
                context, R.attr.preferenceCategoryStyle, R.attr.preferenceCategoryStyle));
    }

    public SelfRemovingPreferenceCategory(Context context) {
        this(context, null);
    }

    @Override
    public void onAttached() {
        super.onAttached();
        mConstraints.onAttached();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mConstraints.onBindViewHolder(holder);
    }

    public void setAvailable(boolean available) {
        mConstraints.setAvailable(available);
    }

    public boolean isAvailable() {
        return mConstraints.isAvailable();
    }
}
