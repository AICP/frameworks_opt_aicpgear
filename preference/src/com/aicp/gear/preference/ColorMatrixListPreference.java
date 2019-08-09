/*
 * Copyright (C) 2019 Android Ice Cold Project
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
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class ColorMatrixListPreference extends ListPreference {

    private CharSequence[] mEntryPreviews;
    private CharSequence[] mEntryPreviewsLight;

    public ColorMatrixListPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                     int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ColorListPreference, defStyleAttr, defStyleRes);

        mEntryPreviews = a.getTextArray(R.styleable.ColorListPreference_entryPreviews);
        mEntryPreviewsLight = a.getTextArray(R.styleable.ColorListPreference_entryPreviewsLight);

        a.recycle();
    }

    public ColorMatrixListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorMatrixListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public ColorMatrixListPreference(Context context) {
        this(context, null);
    }

    public CharSequence[] getEntryPreviews() {
        return mEntryPreviews;
    }

    public CharSequence[] getEntryPreviewsLight() {
        return mEntryPreviewsLight;
    }

}
