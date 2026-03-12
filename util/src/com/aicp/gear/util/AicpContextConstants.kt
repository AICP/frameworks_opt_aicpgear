/*
 * SPDX-FileCopyrightText: 2015, The CyanogenMod Project
 * SPDX-FileCopyrightText: 2015-2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.aicp.gear.util

import android.annotation.SdkConstant

/**
 * @hide
 * TODO: We need to somehow make these managers accessible via getSystemService
 */
object AicpContextConstants {

    /**
     * Features supported by Aicp
     */
    object Features {

        /**
         * Feature for PackageManager#getSystemAvailableFeatures and
         * PackageManager#hasSystemFeature: The device includes the Lineage
         * in-screen fingerprint.
         */
        @JvmField
        @SdkConstant(SdkConstant.SdkConstantType.FEATURE)
        val FOD: String = "vendor.lineage.biometrics.fingerprint.inscreen"
    }
}
