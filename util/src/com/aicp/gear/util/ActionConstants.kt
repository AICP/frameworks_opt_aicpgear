/*
 * SPDX-FileCopyrightText: 2013 SlimRoms Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

object ActionConstants {

    // key must fit with the values arrays from Settings to use
    // SlimActions.java actions
    const val ACTION_HOME = "**home**"
    const val ACTION_BACK = "**back**"
    const val ACTION_SEARCH = "**search**"
    const val ACTION_VOICE_SEARCH = "**voice_search**"
    const val ACTION_MENU = "**menu**"
    const val ACTION_MENU_BIG = "**menu_big**"
    const val ACTION_POWER = "**power**"
    const val ACTION_NOTIFICATIONS = "**notifications**"
    const val ACTION_RECENTS = "**recents**"
    const val ACTION_SCREENSHOT = "**screenshot**"
    const val ACTION_IME = "**ime**"
    const val ACTION_LAST_APP = "**lastapp**"
    const val ACTION_KILL = "**kill**"
    const val ACTION_ASSIST = "**assist**"
    const val ACTION_VIB = "**ring_vib**"
    const val ACTION_SILENT = "**ring_silent**"
    const val ACTION_VIB_SILENT = "**ring_vib_silent**"
    const val ACTION_POWER_MENU = "**power_menu**"
    const val ACTION_TORCH = "**torch**"
    const val ACTION_EXPANDED_DESKTOP = "**expanded_desktop**"
    const val ACTION_THEME_SWITCH = "**theme_switch**"
    const val ACTION_KEYGUARD_SEARCH = "**keyguard_search**"
    const val ACTION_PIE = "**pie**"
    const val ACTION_NAVBAR = "**nav_bar**"
    const val ACTION_IME_NAVIGATION_LEFT = "**ime_nav_left**"
    const val ACTION_IME_NAVIGATION_RIGHT = "**ime_nav_right**"
    const val ACTION_IME_NAVIGATION_UP = "**ime_nav_up**"
    const val ACTION_IME_NAVIGATION_DOWN = "**ime_nav_down**"
    const val ACTION_CAMERA = "**camera**"
    const val ACTION_MEDIA_PREVIOUS = "**media_previous**"
    const val ACTION_MEDIA_NEXT = "**media_next**"
    const val ACTION_MEDIA_PLAY_PAUSE = "**media_play_pause**"
    const val ACTION_WAKE_DEVICE = "**wake_device**"

    // no action
    const val ACTION_NULL = "**null**"

    // shortcut constant used to identify custom apps
    const val ACTION_APP = "**app**"

    const val ICON_EMPTY = "empty"
    const val SYSTEM_ICON_IDENTIFIER = "system_shortcut="
    const val ACTION_DELIMITER = "|"

    const val NAVIGATION_CONFIG_DEFAULT = ACTION_BACK + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY + ACTION_DELIMITER +
            ACTION_HOME + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY + ACTION_DELIMITER +
            ACTION_RECENTS + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY

    const val NAV_RING_CONFIG_DEFAULT = ACTION_ASSIST + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY

    const val PIE_SECOND_LAYER_CONFIG_DEFAULT = ACTION_POWER_MENU + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY + ACTION_DELIMITER +
            ACTION_NOTIFICATIONS + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY + ACTION_DELIMITER +
            ACTION_SEARCH + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY + ACTION_DELIMITER +
            ACTION_SCREENSHOT + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY + ACTION_DELIMITER +
            ACTION_IME + ACTION_DELIMITER +
            ACTION_NULL + ACTION_DELIMITER +
            ICON_EMPTY
}
