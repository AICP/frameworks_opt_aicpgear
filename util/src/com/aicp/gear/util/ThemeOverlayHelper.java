package com.aicp.gear.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThemeOverlayHelper {

    private static final String TAG = ThemeOverlayHelper.class.getSimpleName();

    public static final String NOVERLAY_PKG = "default_pkg";

    private final IOverlayManager mOverlayService;
    private final PackageManager mPackageManager;
    private final Context mContext;

    private final String mCategory;
    private final String mTargets[];

    /**
     * @param target
     * Can be a semicolon-separated list of multiple targets.
     * For multiple targets, each overlay needs to contain the target package name once in its
     * own package name to detect overlays belonging together.
     */
    public ThemeOverlayHelper(Context context, String category, String target) {
        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
        mContext = context;
        mPackageManager = context.getPackageManager();
        mCategory = category;
        mTargets = target.split(";");
    }

    private boolean isTheme(OverlayInfo oi) {
        if (!mCategory.equals(oi.category)) {
            return false;
        }
        try {
            PackageInfo pi = mPackageManager.getPackageInfo(oi.packageName, 0);
            return pi != null && !pi.isStaticOverlayPackage();
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public String getCurrentTheme() {
        String[] themePackages = getAvailableThemes(true /* currentThemeOnly */, mTargets[0]);
        return themePackages.length < 1 ? NOVERLAY_PKG : themePackages[0];
    }

    public String[] getAvailableThemes() {
        return getAvailableThemes(false, mTargets[0]);
    }

    private String[] getAvailableThemes(boolean currentThemeOnly, String target) {
        List<OverlayInfo> infos;
        List<String> pkgs;
        try {
            infos = mOverlayService.getOverlayInfosForTarget(target, UserHandle.myUserId());
            pkgs = new ArrayList<>(infos.size());
            pkgs.add(NOVERLAY_PKG);
            for (int i = 0, size = infos.size(); i < size; i++) {
                if (isTheme(infos.get(i))) {
                    if (infos.get(i).isEnabled() && currentThemeOnly) {
                        return new String[] {infos.get(i).packageName};
                    } else {
                        pkgs.add(infos.get(i).packageName);
                    }
                }
            }
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }

        // Current enabled theme is not found.
        if (currentThemeOnly) {
            return new String[0];
        }
        return pkgs.toArray(new String[pkgs.size()]);
    }

    public boolean setTheme(String value) {
        String current = getCurrentTheme();
        if (Objects.equals(value, current)) {
            return true;
        }
        if (NOVERLAY_PKG.equals(value) && TextUtils.isEmpty(current)
                || TextUtils.equals(value, current)) {
            // Already set.
            return true;
        }
        if (NOVERLAY_PKG.equals(value)) {
            try {
                mOverlayService.setEnabled(current, false, UserHandle.myUserId());
            } catch (RemoteException re) {
                re.printStackTrace();
            }
            for (int i = 1; i < mTargets.length; i++) {
                String pkg = current.replace(mTargets[0], mTargets[i]);
                try {
                    mOverlayService.setEnabled(pkg, false, UserHandle.myUserId());
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
        } else {
            try {
                mOverlayService.setEnabledExclusiveInCategory((String) value, UserHandle.myUserId());
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
            for (int i = 1; i < mTargets.length; i++) {
                // Enable package for additional target
                String pkg = ((String) value).replace(mTargets[0], mTargets[i]);
                try {
                    mOverlayService.setEnabled(pkg, true, UserHandle.myUserId());
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
                // Disable previously applied package for additional target
                if (!NOVERLAY_PKG.equals(current)) {
                    pkg = current.replace(mTargets[0], mTargets[i]);
                    try {
                        mOverlayService.setEnabled(pkg, false, UserHandle.myUserId());
                    } catch (RemoteException re) {
                        re.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    public CharSequence getThemeLabel(String value) {
        if (NOVERLAY_PKG.equals(value)) {
            return mContext.getString(R.string.theme_noverlay);
        }
        try {
            return mPackageManager.getApplicationInfo(value, 0)
                    .loadLabel(mPackageManager);
        } catch (NameNotFoundException e) {
            return value;
        }
    }

    public int getThemeColor(String value, String resName) {
        if (NOVERLAY_PKG.equals(value)) {
            return 0;
        }
        try {
            Resources res = mPackageManager.getResourcesForApplication(value);
            int id = res.getIdentifier(resName, "color", value);
            return res.getColor(id, null);
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    public CharSequence[] getThemeLabels(String... pkgs) {
        CharSequence[] labels = new CharSequence[pkgs.length];
        for (int i = 0; i < pkgs.length; i++) {
            labels[i] = getThemeLabel(pkgs[i]);
        }
        return labels;
    }

    public int[] getThemeColors(String resName, String... pkgs) {
        int[] colors = new int[pkgs.length];
        for (int i = 0; i < pkgs.length; i++) {
            colors[i] = getThemeColor(pkgs[i], resName);
        }
        return colors;
    }

}
