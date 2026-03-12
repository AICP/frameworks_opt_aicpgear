/*
 * SPDX-FileCopyrightText: 2014 SlimRoms Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

/**
 * Action configuration object holding click, longpress actions and an icon.
 */
data class ActionConfig(
    var clickAction: String? = null,
    var clickActionDescription: String? = null,
    var longpressAction: String? = null,
    var longpressActionDescription: String? = null,
    var icon: String? = null
) {

    /**
     * Returns a user-friendly string representation.
     */
    override fun toString(): String {
        return clickActionDescription ?: ""
    }
}
