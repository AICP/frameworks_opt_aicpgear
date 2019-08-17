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
import android.content.res.TypedArray;
import android.os.SystemProperties;
import android.os.UserHandle;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import java.util.Set;

import com.aicp.gear.preference.R;
import com.aicp.gear.util.AicpUtils;

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
        setAvailable(checkConstraints());
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
            if (!AicpUtils.resolveIntent(mContext, i)) {
                Graveyard.get(mContext).addTombstone(mPref.getKey());
                mAvailable = false;
            }
        }
    }

    private boolean isNegated(String key) {
        return key != null && key.startsWith("!");
    }

    private boolean checkConstraints() {
        if (mAttrs == null) {
            return true;
        }

        TypedArray a = mContext.getResources().obtainAttributes(mAttrs,
                R.styleable.aicp_SelfRemovingPreference);

        try {

            // Check if the current user is an owner
            boolean rOwner = a.getBoolean(R.styleable.aicp_SelfRemovingPreference_requiresOwner, false);
            if (rOwner && UserHandle.myUserId() != UserHandle.USER_OWNER) {
                return false;
            }

            // Check if a specific package is installed
            String rPackageInst = a.getString(R.styleable.aicp_SelfRemovingPreference_requiresPackageInstalled);
            if (rPackageInst != null) {
                boolean negated = isNegated(rPackageInst);
                if (negated) {
                    rPackageInst = rPackageInst.substring(1);
                }
                boolean available = AicpUtils.isPackageInstalled(mContext, rPackageInst);
                if (available == negated) {
                    return false;
                }
            }

            // Check if a specific package is enabled
            String rPackageEnb = a.getString(R.styleable.aicp_SelfRemovingPreference_requiresPackageEnabled);
            if (rPackageEnb != null) {
                boolean negated = isNegated(rPackageEnb);
                if (negated) {
                    rPackageEnb = rPackageEnb.substring(1);
                }
                boolean available = AicpUtils.isPackageEnabled(rPackageEnb, mContext);
                if (available == negated) {
                    return false;
                }
            }

            // Check if a specific package is available
            String rPackageAvl = a.getString(R.styleable.aicp_SelfRemovingPreference_requiresPackageAvailable);
            if (rPackageAvl != null) {
                boolean negated = isNegated(rPackageAvl);
                if (negated) {
                    rPackageAvl = rPackageAvl.substring(1);
                }
                boolean available = AicpUtils.isPackageAvailable(rPackageAvl, mContext);
                if (available == negated) {
                    return false;
                }
            }

            // Check if an intent can be resolved to handle the given action
            String rAction = a.getString(R.styleable.aicp_SelfRemovingPreference_requiresAction);
            if (rAction != null) {
                boolean negated = isNegated(rAction);
                if (negated) {
                    rAction = rAction.substring(1);
                }
                boolean available = AicpUtils.resolveIntent(mContext, rAction);
                if (available == negated) {
                    return false;
                }
            }

            // Check a boolean system property
            String rProperty = a.getString(R.styleable.aicp_SelfRemovingPreference_requiresProperty);
            if (rProperty != null) {
                boolean negated = isNegated(rProperty);
                if (negated) {
                    rProperty = rProperty.substring(1);
                }
                String value = SystemProperties.get(rProperty);
                boolean available = value != null && Boolean.parseBoolean(value);
                if (available == negated) {
                    return false;
                }
            }

            // Check a config resource. This can be a bool, string or integer.
            // The preference is removed if any of the following are true:
            // * A bool resource is false.
            // * A string resource is null.
            // * An integer resource is zero.
            // * An integer is non-zero and when bitwise logically ANDed with
            //   attribute requiresConfigMask, the result is zero.
            TypedValue tv = a.peekValue(R.styleable.aicp_SelfRemovingPreference_requiresConfig);
            if (tv != null && tv.resourceId != 0) {
                if (tv.type == TypedValue.TYPE_STRING &&
                        mContext.getResources().getString(tv.resourceId) == null) {
                    return false;
                } else if (tv.type == TypedValue.TYPE_INT_BOOLEAN && tv.data == 0) {
                    return false;
                } else if (tv.type == TypedValue.TYPE_INT_DEC) {
                    int mask = a.getInt(
                            R.styleable.aicp_SelfRemovingPreference_requiresConfigMask, -1);
                    if (tv.data == 0 || (mask >= 0 && (tv.data & mask) == 0)) {
                        return false;
                    }
                }
            }
        } finally {
            a.recycle();
        }

        return true;
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
