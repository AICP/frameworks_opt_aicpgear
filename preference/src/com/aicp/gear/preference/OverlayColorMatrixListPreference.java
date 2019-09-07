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
import android.text.TextUtils;
import android.util.AttributeSet;

import com.aicp.gear.util.ThemeOverlayHelper;

public class OverlayColorMatrixListPreference extends ColorMatrixListPreference {

    private String mOverlayCategory;
    private String mOverlayTarget;
    private String mPreviewResourceName;
    private String mPreviewResourceNameLight;

    private ThemeOverlayHelper mThemeOverlayHelper;

    public OverlayColorMatrixListPreference(Context context, AttributeSet attrs, int defStyle,
                                            int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.OverlayPreference, defStyle, defStyleRes);

        mOverlayCategory = a.getString(R.styleable.OverlayPreference_overlayCategory);
        mOverlayTarget = a.getString(R.styleable.OverlayPreference_overlayTarget);
        mPreviewResourceName = a.getString(R.styleable.OverlayPreference_previewResourceName);
        mPreviewResourceNameLight =
                a.getString(R.styleable.OverlayPreference_previewResourceNameLight);

        a.recycle();

        mThemeOverlayHelper = new ThemeOverlayHelper(context, mOverlayCategory, mOverlayTarget);
        setPreferenceDataStore(new OverlaySettingsStore(mThemeOverlayHelper));
        updateEntries();
    }

    public OverlayColorMatrixListPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public OverlayColorMatrixListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public OverlayColorMatrixListPreference(Context context) {
        this(context, null);
    }

    private String[] getThemeColors(String resourceName, String[] values) {
        int[] previews = mThemeOverlayHelper.getThemeColors(resourceName, values);
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = String.valueOf(previews[i]);
        }
        return result;
    }

    private void updateEntries() {
        String[] values = mThemeOverlayHelper.getAvailableThemes();
        CharSequence[] labels = mThemeOverlayHelper.getThemeLabels(values);
        setEntries(labels);
        setEntryValues(values);
        if (!TextUtils.isEmpty(mPreviewResourceName)) {
            setEntryPreviews(getThemeColors(mPreviewResourceName, values));
        }
        if (!TextUtils.isEmpty(mPreviewResourceNameLight)) {
            setEntryPreviewsLight(getThemeColors(mPreviewResourceNameLight, values));
        }
    }

}
