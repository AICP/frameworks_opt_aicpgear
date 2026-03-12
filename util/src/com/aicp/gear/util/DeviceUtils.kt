package com.aicp.gear.util

import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.hardware.display.WifiDisplayStatus
import android.nfc.NfcAdapter
import android.os.Vibrator
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.DisplayCutout
import android.view.DisplayInfo
import android.view.WindowManager
import java.util.*

object DeviceUtils {
    private const val TAG = "AicpGearDeviceUtils"
    private const val DEBUG = false
    private const val SETTINGS_METADATA_NAME = "com.android.settings"

    // Device types
    private const val DEVICE_PHONE = 0
    private const val DEVICE_HYBRID = 1
    private const val DEVICE_TABLET = 2
    private const val NO_CUTOUT = -1

    @JvmStatic
    fun deviceSupportsRemoteDisplay(ctx: Context): Boolean {
        val dm = ctx.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        return dm.wifiDisplayStatus.featureState != WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE
    }

    @JvmStatic
    fun deviceSupportsBluetooth(): Boolean {
        return BluetoothAdapter.getDefaultAdapter() != null
    }

    @JvmStatic
    fun deviceSupportsNfc(context: Context): Boolean {
        return NfcAdapter.getDefaultAdapter(context) != null
    }

    @JvmStatic
    fun deviceSupportsGps(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    }

    @JvmStatic
    fun adbEnabled(resolver: ContentResolver): Boolean {
        return Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED, 0) == 1
    }

    @JvmStatic
    fun deviceSupportsVibrator(ctx: Context): Boolean {
        val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        return vibrator.hasVibrator()
    }

    @JvmStatic
    fun deviceSupportsTorch(context: Context): Boolean {
        // Need to be adapted to new torch API
        return true
    }

    @JvmStatic
    fun filterUnsupportedDeviceFeatures(
        context: Context,
        valuesArray: Array<String>?,
        entriesArray: Array<String>?
    ): FilteredDeviceFeaturesArray? {
        if (valuesArray == null || entriesArray == null) {
            return null
        }
        val finalEntries: MutableList<String> = ArrayList()
        val finalValues: MutableList<String> = ArrayList()
        for (i in valuesArray.indices) {
            if (isSupportedFeature(context, valuesArray[i])) {
                finalEntries.add(entriesArray[i])
                finalValues.add(valuesArray[i])
            }
        }
        return FilteredDeviceFeaturesArray(
            finalEntries.toTypedArray(),
            finalValues.toTypedArray()
        )
    }

    private fun isSupportedFeature(context: Context, action: String): Boolean {
        return !(action == ActionConstants.ACTION_TORCH && !deviceSupportsTorch(context) ||
                action == ActionConstants.ACTION_VIB && !deviceSupportsVibrator(context) ||
                action == ActionConstants.ACTION_VIB_SILENT && !deviceSupportsVibrator(context))
    }

    class FilteredDeviceFeaturesArray(
        var entries: Array<String>,
        var values: Array<String>
    )

    private fun getScreenType(con: Context): Int {
        val wm = con.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outDisplayInfo = DisplayInfo()
        wm.defaultDisplay.getDisplayInfo(outDisplayInfo)
        val shortSize = Math.min(outDisplayInfo.logicalHeight, outDisplayInfo.logicalWidth)
        val shortSizeDp = shortSize * DisplayMetrics.DENSITY_DEFAULT / outDisplayInfo.logicalDensityDpi
        return if (shortSizeDp < 600) {
            DEVICE_PHONE
        } else if (shortSizeDp < 720) {
            DEVICE_HYBRID
        } else {
            DEVICE_TABLET
        }
    }

    @JvmStatic
    fun isPhone(con: Context): Boolean {
        return getScreenType(con) == DEVICE_PHONE
    }

    @JvmStatic
    fun isHybrid(con: Context): Boolean {
        return getScreenType(con) == DEVICE_HYBRID
    }

    @JvmStatic
    fun isTablet(con: Context): Boolean {
        return getScreenType(con) == DEVICE_TABLET
    }

    @JvmStatic
    fun isVoiceCapable(context: Context): Boolean {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephony.isVoiceCapable
    }

    @JvmStatic
    fun deviceSupportsFingerPrint(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }

    @JvmStatic
    fun deviceSupportsFlashLight(context: Context): Boolean {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            val ids = cameraManager.cameraIdList
            for (id in ids) {
                val c = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val lensFacing = c.get(CameraCharacteristics.LENS_FACING)
                if (flashAvailable != null && flashAvailable &&
                    lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true
                }
            }
            false
        } catch (e: CameraAccessException) {
            false
        } catch (e: AssertionError) {
            false
        }
    }

    @JvmStatic
    fun getCutoutType(context: Context): Int {
        val info = DisplayInfo()
        context.display?.getDisplayInfo(info)
        val cutout = info.displayCutout
        if (cutout == null) {
            if (DEBUG) Log.v(TAG, "noCutout")
            return NO_CUTOUT
        }
        val displaySize = android.graphics.Point()
        context.display?.getRealSize(displaySize)
        val cutOutBounds = cutout.boundingRects
        if (cutOutBounds != null) {
            for (cutOutRect in cutOutBounds) {
                if (DEBUG) Log.v(TAG, "cutout left= " + cutOutRect.left)
                if (DEBUG) Log.v(TAG, "cutout right= " + cutOutRect.right)
                if (cutOutRect.left == 0 && cutOutRect.right > 0) {
                    if (DEBUG) Log.v(TAG, "cutout position= " + DisplayCutout.BOUNDS_POSITION_LEFT)
                    return DisplayCutout.BOUNDS_POSITION_LEFT
                } else if (cutOutRect.right == displaySize.x && (displaySize.x - cutOutRect.left) > 0) {
                    if (DEBUG) Log.v(TAG, "cutout position= " + DisplayCutout.BOUNDS_POSITION_RIGHT)
                    return DisplayCutout.BOUNDS_POSITION_RIGHT
                }
            }
        }
        return NO_CUTOUT
    }
}

