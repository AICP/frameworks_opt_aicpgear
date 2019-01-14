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
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.aicp.gear.preference.R;

/**
 * Helpers for checking if a device supports various features.
 *
 * @hide
 */
public class ConstraintsHelper {

    private static final String TAG = "ConstraintsHelper";

    private static final boolean DEBUG = Log.isLoggable(TAG, Log.VERBOSE);

    private final Context mContext;

    private final AttributeSet mAttrs;

    private final Preference mPref;

    private boolean mAvailable = true;

    private String mReplacesKey = null;

    public ConstraintsHelper(Context context, AttributeSet attrs, Preference pref) {
        mContext = context;
        mAttrs = attrs;
        mPref = pref;

        TypedArray a = context.getResources().obtainAttributes(attrs,
                R.styleable.aicp_SelfRemovingPreference);
        mReplacesKey = a.getString(R.styleable.aicp_SelfRemovingPreference_replacesKey);
        Log.d(TAG, "construct key=" + mPref.getKey() + " available=" + mAvailable);
    }

    public void setAvailable(boolean available) {
        mAvailable = available;
        if (!available) {
            Graveyard.get(mContext).addTombstone(mPref.getKey());
        }
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    private PreferenceGroup getParent(Preference preference) {
        return getParent(mPref.getPreferenceManager().getPreferenceScreen(), preference);
    }

    private PreferenceGroup getParent(PreferenceGroup root, Preference preference) {
        for (int i = 0; i < root.getPreferenceCount(); i++) {
            Preference p = root.getPreference(i);
            if (p == preference)
                return root;
            if (PreferenceGroup.class.isInstance(p)) {
                PreferenceGroup parent = getParent((PreferenceGroup) p, preference);
                if (parent != null)
                    return parent;
            }
        }
        return null;
    }

    private void checkIntent() {
        Intent i = mPref.getIntent();
        if (i != null) {
            if (!resolveIntent(mContext, i)) {
                Graveyard.get(mContext).addTombstone(mPref.getKey());
                mAvailable = false;
            }
        }
    }

    /**
     * Checks if a package is available to handle the given action.
     */
    public static boolean resolveIntent(Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "resolveIntent " + Objects.toString(intent));
        // check whether the target handler exist in system
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> results = pm.queryIntentActivitiesAsUser(intent,
                PackageManager.MATCH_SYSTEM_ONLY,
                UserHandle.myUserId());
        for (ResolveInfo resolveInfo : results) {
            // check is it installed in system.img, exclude the application
            // installed by user
            if (DEBUG) Log.d(TAG, "resolveInfo: " + Objects.toString(resolveInfo));
            if ((resolveInfo.activityInfo.applicationInfo.flags &
                    ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean resolveIntent(Context context, String action) {
        return resolveIntent(context, new Intent(action));
    }

    public static int getAttr(Context context, int attr, int fallbackAttr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        if (value.resourceId != 0) {
            return attr;
        }
        return fallbackAttr;
    }

    public void onAttached() {
        checkIntent();

        if (isAvailable() && mReplacesKey != null) {
            Graveyard.get(mContext).addTombstone(mReplacesKey);
        }

        Graveyard.get(mContext).summonReaper(mPref.getPreferenceManager());
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (!isAvailable()) {
            return;
        }
    }

    /**
     * If we want to keep this at the preference level vs the fragment level, we need to
     * collate all the preferences that need to be removed when attached to the
     * hierarchy, then purge them all when loading is complete. The Graveyard keeps track
     * of this, and will reap the dead when onAttached is called.
     */
    private static class Graveyard {

        private Set<String> mDeathRow = new ArraySet<>();

        private static Graveyard sInstance;

        private final Context mContext;

        private Graveyard(Context context) {
            mContext = context;
        }

        public synchronized static Graveyard get(Context context) {
            if (sInstance == null) {
                sInstance = new Graveyard(context);
            }
            return sInstance;
        }

        public void addTombstone(String pref) {
            synchronized (mDeathRow) {
                mDeathRow.add(pref);
            }
        }

        private PreferenceGroup getParent(Preference p1, Preference p2) {
            return getParent(p1.getPreferenceManager().getPreferenceScreen(), p2);
        }

        private PreferenceGroup getParent(PreferenceGroup root, Preference preference) {
            for (int i = 0; i < root.getPreferenceCount(); i++) {
                Preference p = root.getPreference(i);
                if (p == preference)
                    return root;
                if (PreferenceGroup.class.isInstance(p)) {
                    PreferenceGroup parent = getParent((PreferenceGroup) p, preference);
                    if (parent != null)
                        return parent;
                }
            }
            return null;
        }

        private void hidePreference(PreferenceManager mgr, Preference pref) {
            pref.setVisible(false);
            // Hide the group if nothing is visible
            final PreferenceGroup group = getParent(pref, pref);
            boolean allHidden = true;
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                if (group.getPreference(i).isVisible()) {
                    allHidden = false;
                    break;
                }
            }
            if (allHidden) {
                group.setVisible(false);
            }
        }

        public void summonReaper(PreferenceManager mgr) {
            synchronized (mDeathRow) {
                Set<String> notReadyForReap = new ArraySet<>();
                for (String dead : mDeathRow) {
                    Preference deadPref = mgr.findPreference(dead);
                    if (deadPref != null) {
                        hidePreference(mgr, deadPref);
                    } else {
                        notReadyForReap.add(dead);
                    }
                }
                mDeathRow = notReadyForReap;
            }
        }
    }
}
