package com.aicp.gear.util;

import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;

public class ThemeOverlayHelper {

    private static final String TAG = ThemeOverlayHelper.class.getSimpleName();

    private static final String[] DARK_COMMON_OVERLAYS = {
            "com.aicp.overlay.defaultdark.com.android.calculator2",
            "com.aicp.overlay.defaultdark.com.android.contacts",
            "com.aicp.overlay.defaultdark.com.android.deskclock",
            "com.aicp.overlay.defaultdark.com.android.dialer",
            "com.aicp.overlay.defaultdark.com.android.documentsui",
            "com.aicp.overlay.defaultdark.com.android.messaging",
            "com.aicp.overlay.defaultdark.com.android.packageinstaller",
            "com.aicp.overlay.defaultdark.com.android.phone",
            "com.aicp.overlay.defaultdark.com.android.server.telecom",
            "com.aicp.overlay.defaultdark.com.android.settings.intelligence",
            "com.aicp.overlay.defaultdark.com.android.settings",
    };

    private static final String[] DARK_OVERLAYS = {
            "com.aicp.overlay.defaultdark.android",
            "com.aicp.overlay.defaultdark.com.android.systemui",
    };

    private static final String[] BLACK_OVERLAYS = {
            "com.aicp.overlay.defaultblack.android",
            "com.aicp.overlay.defaultblack.com.android.systemui",
    };

    private static final String[] DARK_NOTIF_OVERLAYS = {
            "com.aicp.overlay.defaultdark.notif.android",
            "com.aicp.overlay.defaultdark.notif.com.android.systemui",
    };

    private static final String[] BLACK_NOTIF_OVERLAYS = {
            "com.aicp.overlay.defaultblack.notif.android",
            "com.aicp.overlay.defaultblack.notif.com.android.systemui",
    };

    private static final String[] DARK_TRANSPARENT_OVERLAYS = {
            "com.aicp.overlay.defaultdark.transparent.android",
            "com.aicp.overlay.defaultdark.transparent.com.android.systemui",
    };

    private static final String[] BLACK_TRANSPARENT_OVERLAYS = {
            "com.aicp.overlay.defaultblack.transparent.android",
            "com.aicp.overlay.defaultblack.transparent.com.android.systemui",
    };

    private static final String[] ACCENT_OVERLAYS = {
            "com.aicp.overlay.accent.amber.android",
            "com.aicp.overlay.accent.greenlight.android",
            "com.aicp.overlay.accent.lime.android",
            "com.aicp.overlay.accent.bluelight.android",
            "com.aicp.overlay.accent.cyan.android",
            "com.aicp.overlay.accent.denim.android",
            "com.aicp.overlay.accent.gold.android",
            "com.aicp.overlay.accent.orange.android",
            "com.aicp.overlay.accent.oxygen.android",
            "com.aicp.overlay.accent.pink.android",
            "com.aicp.overlay.accent.pixel.android",
            "com.aicp.overlay.accent.purple.android",
            "com.aicp.overlay.accent.red.android",
            "com.aicp.overlay.accent.teal.android",
            "com.aicp.overlay.accent.turquoise.android",
            "com.aicp.overlay.accent.yellow.android",
            "com.aicp.overlay.accent.grey.android",
            "com.aicp.overlay.accent.carnation.android",
            "com.aicp.overlay.accent.whiteblack.android",
            "com.aicp.overlay.accent.indigo.android",
            "com.aicp.overlay.accent.lava.android",
    };

    private static final String[] ROUND_OVERLAYS = {
            "com.aicp.overlay.round.android",
            "com.aicp.overlay.round.com.android.launcher3",
    };

    private static final String[] NONROUND_OVERLAYS = {
            "com.aicp.overlay.nonround.android",
            "com.aicp.overlay.nonround.com.android.systemui",
            "com.aicp.overlay.nonround.com.android.launcher3",
    };

    private static final String[] OLDICONS_OVERLAYS = {
            "com.aicp.overlay.oldicons.com.android.systemui",
            "com.aicp.overlay.oldicons.com.android.settings",
    };

    private static final String QS_SHAPE_PLAIN_OVERLAY =
            "com.aicp.overlay.qsshape.plain.com.android.systemui";

    private static final String QS_SHAPE_PLAIN_ACCENT_OVERLAY =
            "com.aicp.overlay.qsshape.plainaccent.com.android.systemui";

    private static final String QS_SHAPE_SQUIRCLE_ACCENT_OVERLAY =
            "com.aicp.overlay.qsshape.squircle.com.android.systemui";

    private static final String QS_SHAPE_SQUARE_ACCENT_OVERLAY =
            "com.aicp.overlay.qsshape.square.com.android.systemui";

    private static final String QS_SHAPE_ROUNDED_SQUARE_ACCENT_OVERLAY =
            "com.aicp.overlay.qsshape.roundedsquare.com.android.systemui";

    private static final String QS_SHAPE_HEXAGON_OVERLAY =
            "com.aicp.overlay.qsshape.hexagon.com.android.systemui";

    private static final HashMap<Integer, String> ACCENT_MAP = new HashMap();
    static {
        // Format: settings key, package name
        ACCENT_MAP.put(1, ACCENT_OVERLAYS[0]); // amber
        ACCENT_MAP.put(2, ACCENT_OVERLAYS[1]); // green light
        ACCENT_MAP.put(3, ACCENT_OVERLAYS[2]); // lime
        ACCENT_MAP.put(4, ACCENT_OVERLAYS[3]); // blue light
        ACCENT_MAP.put(5, ACCENT_OVERLAYS[4]); // cyan
        ACCENT_MAP.put(6, ACCENT_OVERLAYS[5]); // denim
        ACCENT_MAP.put(7, ACCENT_OVERLAYS[6]); // gold
        ACCENT_MAP.put(8, ACCENT_OVERLAYS[7]); // orange
        ACCENT_MAP.put(9, ACCENT_OVERLAYS[8]); // oxygen
        ACCENT_MAP.put(10, ACCENT_OVERLAYS[9]); // pink
        ACCENT_MAP.put(11, ACCENT_OVERLAYS[10]); // pixel
        ACCENT_MAP.put(12, ACCENT_OVERLAYS[11]); // purple
        ACCENT_MAP.put(13, ACCENT_OVERLAYS[12]); // red
        ACCENT_MAP.put(14, ACCENT_OVERLAYS[13]); // teal
        ACCENT_MAP.put(15, ACCENT_OVERLAYS[14]); // turquoise
        ACCENT_MAP.put(16, ACCENT_OVERLAYS[15]); // yellow
        ACCENT_MAP.put(17, ACCENT_OVERLAYS[16]); // grey
        ACCENT_MAP.put(18, ACCENT_OVERLAYS[17]); // carnation
        ACCENT_MAP.put(19, ACCENT_OVERLAYS[18]); // white black
        ACCENT_MAP.put(20, ACCENT_OVERLAYS[19]); // indigo
        ACCENT_MAP.put(21, ACCENT_OVERLAYS[20]); // lava
    }

    private static final HashSet<Uri> THEMING_SYSTEM_SETTINGS = new HashSet();
    static {
        /*
        THEMING_SYSTEM_SETTINGS.add(Settings.System.getUriFor(Settings.System.THEMING_BASE));
        THEMING_SYSTEM_SETTINGS.add(Settings.System.getUriFor(Settings.System.THEMING_ACCENT));
        THEMING_SYSTEM_SETTINGS.add(Settings.System.getUriFor(Settings.System.THEMING_CORNERS));
        THEMING_SYSTEM_SETTINGS.add(Settings.System.getUriFor(Settings.System.THEMING_QS_SHAPE));
        THEMING_SYSTEM_SETTINGS.add(Settings.System.getUriFor(
                    Settings.System.THEMING_CONTROL_NIGHT_MODE));
        THEMING_SYSTEM_SETTINGS.add(Settings.System.getUriFor(
                    Settings.System.THEMING_SYSTEM_ICONS_STYLE));
                    */
    }

    private ThemeOverlayHelper() {}

    public static boolean isThemeSystemSetting(Uri uri) {
        return THEMING_SYSTEM_SETTINGS.contains(uri);
    }

    public static boolean updateOverlays(Context context, IOverlayManager om, int userId) {
        boolean changed = false;
        /*
        ContentResolver resolver = context.getContentResolver();
        int baseTheme = Settings.System.getInt(resolver, Settings.System.THEMING_BASE, 0);
        boolean isDarkTheme = isDarkBaseTheme(baseTheme);
        boolean enabled = baseTheme == 1;
        for (String darkOverlay: DARK_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, darkOverlay, enabled);
        }
        enabled = baseTheme == 2;
        for (String blackOverlay: BLACK_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, blackOverlay, enabled);
        }
        enabled = baseTheme == 3;
        for (String blackOverlay: DARK_NOTIF_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, blackOverlay, enabled);
        }
        enabled = baseTheme == 4;
        for (String blackOverlay: BLACK_NOTIF_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, blackOverlay, enabled);
        }
        enabled = baseTheme == 5;
        for (String blackOverlay: DARK_TRANSPARENT_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, blackOverlay, enabled);
        }
        enabled = baseTheme == 6;
        for (String blackOverlay: BLACK_TRANSPARENT_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, blackOverlay, enabled);
        }
        enabled = isDarkTheme;
        for (String darkOverlay: DARK_COMMON_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, darkOverlay, enabled);
        }
        int accentSetting = Settings.System.getInt(resolver, Settings.System.THEMING_ACCENT, 0);
        String accentPackage = ACCENT_MAP.get(accentSetting);
        for (String accentOverlay: ACCENT_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, accentOverlay,
                    accentOverlay.equals(accentPackage));
        }
        int cornerSetting = Settings.System.getInt(resolver, Settings.System.THEMING_CORNERS, 0);
        enabled = cornerSetting == 1;
        for (String roundOverlay: ROUND_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, roundOverlay, enabled);
        }
        enabled = cornerSetting == 2;
        for (String nonRoundOverlay: NONROUND_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, nonRoundOverlay, enabled);
        }
        int systemIconStyleSetting =
                Settings.System.getInt(resolver, Settings.System.THEMING_SYSTEM_ICONS_STYLE, 0);
        enabled = systemIconStyleSetting == 1;
        for (String oldIconsOverlay: OLDICONS_OVERLAYS) {
            changed |= setOverlayEnabled(om, userId, oldIconsOverlay, enabled);
        }
        int qsStyleSetting = Settings.System.getInt(resolver, Settings.System.THEMING_QS_SHAPE, 0);
        changed |= setOverlayEnabled(om, userId, QS_SHAPE_PLAIN_OVERLAY, qsStyleSetting == 1);
        changed |= setOverlayEnabled(om, userId, QS_SHAPE_PLAIN_ACCENT_OVERLAY,
                qsStyleSetting == 2);
        changed |= setOverlayEnabled(om, userId, QS_SHAPE_SQUIRCLE_ACCENT_OVERLAY,
                qsStyleSetting == 3);
        changed |= setOverlayEnabled(om, userId, QS_SHAPE_SQUARE_ACCENT_OVERLAY,
                qsStyleSetting == 4);
        changed |= setOverlayEnabled(om, userId, QS_SHAPE_ROUNDED_SQUARE_ACCENT_OVERLAY,
                qsStyleSetting == 5);
        changed |= setOverlayEnabled(om, userId, QS_SHAPE_HEXAGON_OVERLAY, qsStyleSetting == 6);

        updateNightMode(context, isDarkTheme);
        */

        return changed;
    }

    private static boolean setOverlayEnabled(IOverlayManager om, int userId, String overlay,
                                             boolean enabled) {
        boolean currentlyEnabled = isOverlayEnabled(om, userId, overlay);

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

    private static boolean isOverlayEnabled(IOverlayManager om, int userId, String overlay) {
        OverlayInfo systemuiThemeInfo = null;
        try {
            systemuiThemeInfo = om.getOverlayInfo(overlay, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return systemuiThemeInfo != null && systemuiThemeInfo.isEnabled();
    }

    public static boolean isUsingDarkTheme(IOverlayManager om, int userId) {
        return isOverlayEnabled(om, userId, DARK_OVERLAYS[0]) ||
                isOverlayEnabled(om, userId, BLACK_OVERLAYS[0]);
    }

    public static boolean isDarkBaseTheme(int baseTheme) {
        return baseTheme > 0 && baseTheme <= 6;
    }

    private static boolean hasThemedNotifications(int baseTheme) {
            return baseTheme >= 3 && baseTheme <= 6;
    }

    /**
     * @return
     * True if the theme change requires a SystemUI restart, false otherwise.
     */
    public static boolean doesThemeChangeRequireSystemUIRestart(Context context,
                                                                String preferenceKey,
                                                                Integer previousValue,
                                                                int newValue) {
        /*
        if (previousValue == null) {
            // This means new value was not stored yet, we can still grab the old value
            // (or this method wasn't used properly)
            previousValue = Settings.System.getInt(context.getContentResolver(), preferenceKey, 0);
        }
        if (previousValue == newValue) {
            // Nothing to do
            return false;
        }
        if (Settings.System.THEMING_BASE.equals(preferenceKey)) {
            // If notifications are themed (both previously or as a result),
            // we need to restart SystemUI for changes to have effect
            if (hasThemedNotifications(newValue) || hasThemedNotifications(previousValue)) {
                return true;
            }
            return false;
        } else if (Settings.System.THEMING_CORNERS.equals(preferenceKey)) {
            // Rounded corners of notifications need systemui restart
            return true;
        } else if (Settings.System.THEMING_SYSTEM_ICONS_STYLE.equals(preferenceKey)) {
            // Not sure why this doesn't work without restart, it actually get's updated after
            // a QS shape change without restart...
            return true;
        } else {
            return false;
        }
        */
        return false;
    }

    private static void updateNightMode(Context context, boolean isUsingDarkTheme) {
        /*
        if (Settings.System.getInt(context.getContentResolver(),
                    Settings.System.THEMING_CONTROL_NIGHT_MODE, 1) == 0) {
            // Controlling night mode together with our theme disabled
            return;
        }
        UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        uiManager.setNightMode(isUsingDarkTheme
                ? UiModeManager.MODE_NIGHT_YES
                : UiModeManager.MODE_NIGHT_NO);
                */
    }

}
