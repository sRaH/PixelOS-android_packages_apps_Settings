/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.settings.connecteddevice

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.preference.Preference
import com.android.settings.core.BasePreferenceController

/** Launches the device-provided Miracast sink, when one is installed. */
class CastReceiverPreferenceController(context: Context, preferenceKey: String) :
    BasePreferenceController(context, preferenceKey) {

    override fun getAvailabilityStatus(): Int =
        if (
            mContext.packageManager.resolveActivity(
                receiverIntent(),
                PackageManager.MATCH_SYSTEM_ONLY,
            ) != null
        ) {
            AVAILABLE
        } else {
            UNSUPPORTED_ON_DEVICE
        }

    override fun handlePreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key != preferenceKey) return false

        return try {
            mContext.startActivity(receiverIntent())
            true
        } catch (exception: ActivityNotFoundException) {
            Log.w(TAG, "Cast receiver activity disappeared", exception)
            false
        }
    }

    private fun receiverIntent(): Intent =
        Intent(ACTION_OPEN_RECEIVER)
            .setPackage(RECEIVER_PACKAGE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private companion object {
        const val TAG = "CastReceiverPrefCtrl"
        const val RECEIVER_PACKAGE = "com.android.wfdreceiver"
        const val ACTION_OPEN_RECEIVER = "com.android.wfdreceiver.action.OPEN"
    }
}
