/*
 * Copyright (C) 2019 Android Ice Cold Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aicp.gear.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.aicp.gear.util.ThemeOverlayHelper;

public class OverlayPreference extends ListPreference {

    private String mOverlayCategory;
    private String mOverlayTarget;

    private ThemeOverlayHelper mThemeOverlayHelper;

    public OverlayPreference(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.OverlayPreference, defStyle, defStyleRes);

        mOverlayCategory = a.getString(R.styleable.OverlayPreference_overlayCategory);
        mOverlayTarget = a.getString(R.styleable.OverlayPreference_overlayTarget);

        a.recycle();

        mThemeOverlayHelper = new ThemeOverlayHelper(context, mOverlayCategory, mOverlayTarget);
        updateEntries();
    }

    public OverlayPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public OverlayPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public OverlayPreference(Context context) {
        this(context, null);
    }

    private void updateEntries() {
        String[] values = mThemeOverlayHelper.getAvailableThemes();
        CharSequence[] labels = mThemeOverlayHelper.getThemeLabels(values);
        setEntries(labels);
        setEntryValues(values);
    }

    @Override
    protected boolean persistString(String value) {
        return mThemeOverlayHelper.setTheme(value);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return mThemeOverlayHelper.getCurrentTheme();
    }
}
