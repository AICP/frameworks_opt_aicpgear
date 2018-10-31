package com.aicp.gear.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;

public class ThemeOverlayHelper {

    private static final String TAG = ThemeOverlayHelper.class.getSimpleName();

    private static final String[] DARK_COMMON_OVERLAYS = {
            "com.aicp.overlay.defaultdark.com.android.settings",
            "com.aicp.overlay.defaultdark.com.android.calculator2",
    };

    private static final String[] DARK_OVERLAYS = {
            "com.aicp.overlay.defaultdark.android",
            "com.aicp.overlay.defaultdark.com.android.systemui",
    };

    private static final String[] BLACK_OVERLAYS = {
            "com.aicp.overlay.defaultblack.android",
            "com.aicp.overlay.defaultblack.com.android.systemui",
    };

    private static final String[] ACCENT_OVERLAYS = {
            "com.aicp.overlay.accent.amber.android",
            "com.aicp.overlay.accent.greenlight.android",
            "com.aicp.overlay.accent.lime.android",
    };

    private static final HashMap<Integer, String> ACCENT_MAP = new HashMap();
    static {
        // Format: settings key, package name
        ACCENT_MAP.put(1, ACCENT_OVERLAYS[0]);
        ACCENT_MAP.put(2, ACCENT_OVERLAYS[1]);
        ACCENT_MAP.put(3, ACCENT_OVERLAYS[2]);
    }

    private ThemeOverlayHelper() {}

    public static boolean updateOverlays(Context context, IOverlayManager om, int userId) {
        boolean changed = false;
        ContentResolver resolver = context.getContentResolver();
        int baseTheme = Settings.System.getInt(resolver, Settings.System.THEMING_BASE, 0);
        boolean darkTheme = baseTheme == 1;
        for (String darkOverlay: DARK_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, darkOverlay, darkTheme);
        }
        boolean blackTheme = baseTheme == 2;
        for (String blackOverlay: BLACK_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, blackOverlay, blackTheme);
        }
        for (String darkOverlay: DARK_COMMON_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, darkOverlay, darkTheme||blackTheme);
        }
        int accentSetting = Settings.System.getInt(resolver, Settings.System.THEMING_ACCENT, 0);
        String accentPackage = ACCENT_MAP.get(accentSetting);
        for (String accentOverlay: ACCENT_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, accentOverlay,
                    accentOverlay.equals(accentPackage));
        }
        return changed;
    }

    private static boolean setOverlayEnabled(IOverlayManager om, int userId, String overlay,
                                             boolean enabled) {
        OverlayInfo systemuiThemeInfo = null;
        try {
            systemuiThemeInfo = om.getOverlayInfo(overlay, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        boolean currentlyEnabled = systemuiThemeInfo != null && systemuiThemeInfo.isEnabled();

        if (currentlyEnabled != enabled) {
            try {
                om.setEnabled(overlay, enabled, userId);
                return true;
            } catch (RemoteException e) {
                Log.w(TAG, "Can't change theme for " + overlay, e);
            }
        }
        return false;
    }
}
