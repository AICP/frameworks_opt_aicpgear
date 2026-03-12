/*
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.util

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.hardware.input.InputManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.session.MediaSessionLegacyHelper
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.WindowManagerGlobal
import com.android.internal.statusbar.IStatusBarService
import java.net.URISyntaxException

object Action {
    private const val MSG_INJECT_KEY_DOWN = 1066
    private const val MSG_INJECT_KEY_UP = 1067

    @JvmStatic
    fun processAction(context: Context, action: String?, isLongpress: Boolean) {
        processActionWithOptions(context, action, isLongpress, true)
    }

    @JvmStatic
    fun processActionWithOptions(
        context: Context,
        action: String?,
        isLongpress: Boolean,
        collapseShade: Boolean
    ) {
        if (action == null || action == ActionConstants.ACTION_NULL) {
            return
        }

        var isKeyguardShowing = false
        try {
            isKeyguardShowing = WindowManagerGlobal.getWindowManagerService()?.isKeyguardLocked ?: false
        } catch (e: RemoteException) {
            Log.w("Action", "Error getting window manager service", e)
        }

        val barService = IStatusBarService.Stub.asInterface(
            ServiceManager.getService(Context.STATUS_BAR_SERVICE)
        )
        if (barService == null) {
            return
        }

        when (action) {
            ActionConstants.ACTION_HOME -> {
                triggerVirtualKeypress(KeyEvent.KEYCODE_HOME, isLongpress)
                return
            }
            ActionConstants.ACTION_BACK -> {
                triggerVirtualKeypress(KeyEvent.KEYCODE_BACK, isLongpress)
                return
            }
            ActionConstants.ACTION_SEARCH -> {
                triggerVirtualKeypress(KeyEvent.KEYCODE_SEARCH, isLongpress)
                return
            }
            ActionConstants.ACTION_MENU, ActionConstants.ACTION_MENU_BIG -> {
                triggerVirtualKeypress(KeyEvent.KEYCODE_MENU, isLongpress)
                return
            }
            ActionConstants.ACTION_IME_NAVIGATION_LEFT -> {
                triggerVirtualKeypress(KeyEvent.KEYCODE_DPAD_LEFT, isLongpress)
                return
            }
            ActionConstants.ACTION_IME_NAVIGATION_RIGHT -> {
                triggerVirtualKeypress(KeyEvent.KEYCODE_DPAD_RIGHT, isLongpress)
                return
            }
            ActionConstants.ACTION_IME_NAVIGATION_UP -> {
                triggerVirtualKeypress(KeyEvent.KEYCODE_DPAD_UP, isLongpress)
                return
            }
            ActionConstants.ACTION_IME_NAVIGATION_DOWN -> {
                triggerVirtualKeypress(KeyEvent.KEYCODE_DPAD_DOWN, isLongpress)
                return
            }
            ActionConstants.ACTION_POWER -> {
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                pm.goToSleep(SystemClock.uptimeMillis())
                return
            }
            ActionConstants.ACTION_IME -> {
                if (isKeyguardShowing) {
                    return
                }
                context.sendBroadcastAsUser(
                    Intent("android.settings.SHOW_INPUT_METHOD_PICKER"),
                    UserHandle(UserHandle.USER_CURRENT)
                )
                return
            }
            ActionConstants.ACTION_VOICE_SEARCH -> {
                val intent = Intent(Intent.ACTION_SEARCH_LONG_PRESS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    val searchManager =
                        context.getSystemService(Context.SEARCH_SERVICE) as? android.app.SearchManager
                    searchManager?.stopSearch()
                    startActivity(context, intent, barService, isKeyguardShowing)
                } catch (e: ActivityNotFoundException) {
                    Log.e("SlimActions:", "No activity to handle assist long press action.", e)
                }
                return
            }
            ActionConstants.ACTION_VIB -> {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (ActivityManager.isSystemReady()) {
                    if (am.ringerMode != AudioManager.RINGER_MODE_VIBRATE) {
                        am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        vib?.vibrate(50)
                    } else {
                        am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                        val tg = ToneGenerator(
                            AudioManager.STREAM_NOTIFICATION,
                            (ToneGenerator.MAX_VOLUME * 0.85).toInt()
                        )
                        tg?.startTone(ToneGenerator.TONE_PROP_BEEP)
                    }
                }
                return
            }
            ActionConstants.ACTION_SILENT -> {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (ActivityManager.isSystemReady()) {
                    if (am.ringerMode != AudioManager.RINGER_MODE_SILENT) {
                        am.ringerMode = AudioManager.RINGER_MODE_SILENT
                    } else {
                        am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                        val tg = ToneGenerator(
                            AudioManager.STREAM_NOTIFICATION,
                            (ToneGenerator.MAX_VOLUME * 0.85).toInt()
                        )
                        tg?.startTone(ToneGenerator.TONE_PROP_BEEP)
                    }
                }
                return
            }
            ActionConstants.ACTION_VIB_SILENT -> {
                val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (ActivityManager.isSystemReady()) {
                    when (am.ringerMode) {
                        AudioManager.RINGER_MODE_NORMAL -> {
                            am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                            val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            vib?.vibrate(50)
                        }
                        AudioManager.RINGER_MODE_VIBRATE -> {
                            am.ringerMode = AudioManager.RINGER_MODE_SILENT
                        }
                        else -> {
                            am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                            val tg = ToneGenerator(
                                AudioManager.STREAM_NOTIFICATION,
                                (ToneGenerator.MAX_VOLUME * 0.85).toInt()
                            )
                            tg?.startTone(ToneGenerator.TONE_PROP_BEEP)
                        }
                    }
                }
                return
            }
            ActionConstants.ACTION_CAMERA -> {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA, null)
                startActivity(context, intent, barService, isKeyguardShowing)
                return
            }
            ActionConstants.ACTION_MEDIA_PREVIOUS -> {
                dispatchMediaKeyWithWakeLock(KeyEvent.KEYCODE_MEDIA_PREVIOUS, context)
                return
            }
            ActionConstants.ACTION_MEDIA_NEXT -> {
                dispatchMediaKeyWithWakeLock(KeyEvent.KEYCODE_MEDIA_NEXT, context)
                return
            }
            ActionConstants.ACTION_MEDIA_PLAY_PAUSE -> {
                dispatchMediaKeyWithWakeLock(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, context)
                return
            }
            ActionConstants.ACTION_WAKE_DEVICE -> {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!powerManager.isInteractive) {
                    powerManager.wakeUp(SystemClock.uptimeMillis())
                }
                return
            }
            else -> {
                try {
                    val intent = Intent.parseUri(action, 0)
                    startActivity(context, intent, barService, isKeyguardShowing)
                } catch (e: URISyntaxException) {
                    Log.e("SlimActions:", "URISyntaxException: [$action]")
                } catch (e: ActivityNotFoundException) {
                    Log.e("SlimActions:", "ActivityNotFoundException: [$action]")
                }
                return
            }
        }
    }

    @JvmStatic
    fun isActionKeyEvent(action: String?): Boolean {
        return action == ActionConstants.ACTION_HOME ||
                action == ActionConstants.ACTION_BACK ||
                action == ActionConstants.ACTION_SEARCH ||
                action == ActionConstants.ACTION_MENU ||
                action == ActionConstants.ACTION_MENU_BIG ||
                action == ActionConstants.ACTION_NULL
    }

    private fun startActivity(
        context: Context,
        intent: Intent?,
        barService: IStatusBarService?,
        isKeyguardShowing: Boolean
    ) {
        if (intent == null) {
            return
        }

        try {
            WindowManagerGlobal.getWindowManagerService()?.dismissKeyguard(null, null)
        } catch (e: Exception) {
            Log.w("Action", "Error dismissing keyguard", e)
        }

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
        context.startActivityAsUser(intent, UserHandle(UserHandle.USER_CURRENT))
    }

    private fun dispatchMediaKeyWithWakeLock(keycode: Int, context: Context) {
        if (ActivityManager.isSystemReady()) {
            val event = KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                keycode,
                0
            )
            MediaSessionLegacyHelper.getHelper(context).sendMediaButtonEvent(event, true)
            val upEvent = KeyEvent.changeAction(event, KeyEvent.ACTION_UP)
            MediaSessionLegacyHelper.getHelper(context).sendMediaButtonEvent(upEvent, true)
        }
    }

    @JvmStatic
    fun triggerVirtualKeypress(keyCode: Int, longpress: Boolean) {
        val now = SystemClock.uptimeMillis()
        var downflags = 0
        var upflags = 0
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
            keyCode == KeyEvent.KEYCODE_DPAD_UP ||
            keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            downflags = KeyEvent.FLAG_SOFT_KEYBOARD or KeyEvent.FLAG_KEEP_TOUCH_MODE
            upflags = downflags
        } else {
            downflags = KeyEvent.FLAG_FROM_SYSTEM or KeyEvent.FLAG_VIRTUAL_HARD_KEY
            upflags = downflags
        }
        if (longpress) {
            downflags = downflags or KeyEvent.FLAG_LONG_PRESS
        }

        val downEvent = KeyEvent(
            now, now, KeyEvent.ACTION_DOWN,
            keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
            downflags, InputDevice.SOURCE_KEYBOARD
        )

        val upEvent = KeyEvent(
            now, now, KeyEvent.ACTION_UP,
            keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
            upflags, InputDevice.SOURCE_KEYBOARD
        )
    }
}

