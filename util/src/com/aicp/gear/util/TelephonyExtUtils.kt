/*
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.IBinder
import android.os.RemoteException
import android.os.ServiceManager
import android.os.SystemProperties
import android.telephony.SubscriptionManager
import android.util.Log
import com.android.internal.telephony.PhoneConstants
import org.codeaurora.internal.IExtTelephony
import java.util.concurrent.TimeoutException
import java.util.concurrent.TimeUnit

object TelephonyExtUtils {
    private const val DEBUG = false
    private const val TAG = "TelephonyExtUtils"

    const val ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED =
        "org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED"
    const val EXTRA_NEW_PROVISION_STATE = "newProvisionState"

    private const val ACTIVATE_TIME_OUT = 15000
    private const val PROP_TIME_OUT = "sys.uicc.activate.timeout"

    const val CARD_NOT_PRESENT = -2
    const val INVALID_STATE = -1
    const val NOT_PROVISIONED = 0
    const val PROVISIONED = 1

    private var mNoServiceAvailable = false
    private var mExtTelephony: IExtTelephony? = null

    private val mListeners = mutableListOf<ProvisioningChangedListener>()

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> {
                    if (DEBUG) Log.d(TAG, "Boot completed, registering service")
                    mNoServiceAvailable = getService() == null
                }
                ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED -> {
                    val slotId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
                        SubscriptionManager.INVALID_SIM_SLOT_INDEX)
                    val provisioned = intent.getIntExtra(EXTRA_NEW_PROVISION_STATE,
                        NOT_PROVISIONED) == PROVISIONED
                    if (DEBUG) Log.d(TAG, "Received ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED" +
                            " on slotId: $slotId, sub provisioned: $provisioned")
                    notifyListeners(slotId, provisioned)
                }
            }
        }
    }

    private val mDeathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            if (DEBUG) Log.d(TAG, "Binder died")
            synchronized(TelephonyExtUtils::class.java) {
                mExtTelephony?.asBinder()?.unlinkToDeath(this, 0)
                mExtTelephony = null
            }
        }
    }

    fun getInstance(context: Context): TelephonyExtUtils {
        if (DEBUG) Log.d(TAG, "Registering listeners!")
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED)
            addAction(Intent.ACTION_BOOT_COMPLETED)
        }
        context.applicationContext.registerReceiver(mReceiver, intentFilter)
        return this
    }

    fun hasService(): Boolean {
        return getService() != null
    }

    fun isSubProvisioned(subId: Int): Boolean {
        return isSlotProvisioned(SubscriptionManager.getSlotIndex(subId))
    }

    fun isSlotProvisioned(slotId: Int): Boolean {
        return getCurrentUiccCardProvisioningStatus(slotId) == PROVISIONED
    }

    fun getCurrentUiccCardProvisioningStatus(slotId: Int): Int {
        val service = getService()
        /*if (service != null && slotId != SubscriptionManager.INVALID_SIM_SLOT_INDEX) {
            try {
                return mExtTelephony!!.getCurrentUiccCardProvisioningStatus(slotId)
            } catch (ex: RemoteException) {
                Log.e(TAG, "Failed to get provisioning status for slotId: $slotId", ex)
            }
        }*/
        return INVALID_STATE
    }

    fun activateUiccCard(slotId: Int): Int {
        return setUiccCardProvisioningStatus(PROVISIONED, slotId)
    }

    fun deactivateUiccCard(slotId: Int): Int {
        return setUiccCardProvisioningStatus(NOT_PROVISIONED, slotId)
    }

    private fun setUiccCardProvisioningStatus(provStatus: Int, slotId: Int): Int {
        val actionStr = when (provStatus) {
            PROVISIONED -> "Activating"
            NOT_PROVISIONED -> "Deactivating"
            else -> {
                Log.e(TAG, "Invalid argument for setUiccCardProvisioningStatus (provStatus=$provStatus, slotId=$slotId)")
                return -1
            }
        }

        val service = getService()
        if (service == null) {
            return -1
        }

        val task = object : AsyncTask<Int, Void, Int>() {
            override fun doInBackground(vararg params: Int?): Int {
                /*try {
                    return if (params[0] == PROVISIONED)
                        mExtTelephony!!.activateUiccCard(params[1]!!)
                    else
                        mExtTelephony!!.deactivateUiccCard(params[1]!!)
                } catch (ex: RemoteException) {
                    Log.e(TAG, "$actionStr sub failed for slotId: ${params[1]}")
                }*/
                return -1
            }
        }

        try {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, provStatus, slotId)
                .get(ACTIVATE_TIME_OUT.toLong(), TimeUnit.MILLISECONDS)
        } catch (ex: TimeoutException) {
            Log.e(TAG, "$actionStr sub timed out for slotId: $slotId")
            SystemProperties.set(PROP_TIME_OUT, (slotId + 1).toString())
        } catch (ex: Exception) {
            Log.e(TAG, "$actionStr sub task failed for slotId: $slotId")
        }

        return -1
    }

    fun addListener(listener: ProvisioningChangedListener) {
        if (listener != null) {
            mListeners.remove(listener)
            mListeners.add(listener)
        }
    }

    fun removeListener(listener: ProvisioningChangedListener) {
        mListeners.remove(listener)
    }

    private fun notifyListeners(slotId: Int, provisioned: Boolean) {
        for (listener in mListeners) {
            listener.onProvisioningChanged(slotId, provisioned)
        }
    }

    private fun getService(): IExtTelephony? {
        if (mNoServiceAvailable) {
            if (DEBUG) Log.v(TAG, "Already tried to get a service without success, returning!")
            return null
        }

        if (DEBUG) Log.d(TAG, "Retrieving the service")

        if (mExtTelephony != null) {
            if (DEBUG) Log.d(TAG, "Returning cached service instance")
            return mExtTelephony
        }

        synchronized(TelephonyExtUtils::class.java) {
            try {
                mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"))
                mExtTelephony?.asBinder()?.linkToDeath(mDeathRecipient, 0)
            } catch (ex: NoClassDefFoundError) {
                Log.d(TAG, "Failed to get telephony extension service!")
                mNoServiceAvailable = true
            } catch (ex: RemoteException) {
                Log.d(TAG, "linkToDeath failed!")
            }
        }

        return mExtTelephony
    }

    interface ProvisioningChangedListener {
        fun onProvisioningChanged(slotId: Int, isProvisioned: Boolean)
    }
}

