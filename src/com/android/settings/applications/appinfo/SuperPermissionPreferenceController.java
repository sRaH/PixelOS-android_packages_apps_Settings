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

package com.android.settings.applications.appinfo;

import android.app.AlertDialog;
import android.app.AppGlobals;
import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.android.settings.R;

/** Controls the per-app Super permissions override shown on the App info screen. */
public final class SuperPermissionPreferenceController extends AppInfoPreferenceControllerBase
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SuperPermissionPref";

    public SuperPermissionPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        try {
            ((TwoStatePreference) preference).setChecked(
                    AppGlobals.getPackageManager().isSuperPermissionEnabled(
                            getPackageName(), getUserId()));
        } catch (RemoteException | RuntimeException e) {
            Log.e(TAG, "Unable to read Super permissions state", e);
            preference.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean enabled = (boolean) newValue;
        if (!enabled) {
            return setEnabled(false);
        }

        final CharSequence appLabel = mParent.getAppEntry().label;
        new AlertDialog.Builder(mParent.requireContext())
                .setTitle(mContext.getString(R.string.super_permission_warning_title, appLabel))
                .setMessage(R.string.super_permission_warning_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.super_permission_allow, (dialog, which) -> {
                    if (setEnabled(true)) {
                        ((TwoStatePreference) mPreference).setChecked(true);
                    }
                })
                .show();
        return false;
    }

    private boolean setEnabled(boolean enabled) {
        try {
            AppGlobals.getPackageManager().setSuperPermissionEnabled(
                    getPackageName(), getUserId(), enabled);
            return true;
        } catch (RemoteException | RuntimeException e) {
            Log.e(TAG, "Unable to change Super permissions state", e);
            return false;
        }
    }

    private int getUserId() {
        return UserHandle.getUserId(mParent.getAppEntry().info.uid);
    }

    private String getPackageName() {
        return mParent.getPackageInfo().packageName;
    }
}
