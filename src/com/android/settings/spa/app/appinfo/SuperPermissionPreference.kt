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

package com.android.settings.spa.app.appinfo

import android.app.AppGlobals
import android.content.pm.ApplicationInfo
import android.os.UserHandle
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.settings.R
import com.android.settingslib.spa.framework.compose.OverridableFlow
import com.android.settingslib.spa.widget.dialog.AlertDialogButton
import com.android.settingslib.spa.widget.dialog.rememberAlertDialogPresenter
import com.android.settingslib.spa.widget.preference.SwitchPreference
import com.android.settingslib.spa.widget.preference.SwitchPreferenceModel
import kotlinx.coroutines.flow.flow

/** Super permissions toggle for the SPA App info screen. */
@Composable
fun SuperPermissionPreference(app: ApplicationInfo) {
    val context = LocalContext.current
    val presenter = remember(app) { SuperPermissionPresenter(app) }
    val isChecked = presenter.isCheckedFlow.collectAsStateWithLifecycle(initialValue = null)
    val confirmDialog =
        rememberAlertDialogPresenter(
            confirmButton =
                AlertDialogButton(
                    text = stringResource(R.string.super_permission_allow),
                    onClick = { presenter.setEnabled(true) },
                ),
            dismissButton = AlertDialogButton(stringResource(R.string.cancel)),
            title =
                stringResource(
                    R.string.super_permission_warning_title,
                    app.loadLabel(context.packageManager),
                ),
            text = { Text(stringResource(R.string.super_permission_warning_message)) },
        )

    SwitchPreference(
        remember {
            object : SwitchPreferenceModel {
                override val title = context.getString(R.string.super_permission_title)
                override val summary = {
                    context.getString(R.string.super_permission_summary)
                }
                override val checked = { isChecked.value }
                override val onCheckedChange: (Boolean) -> Unit = { enabled ->
                    if (enabled) confirmDialog.open() else presenter.setEnabled(false)
                }
            }
        }
    )
}

private class SuperPermissionPresenter(private val app: ApplicationInfo) {
    private val packageManager = AppGlobals.getPackageManager()
    private val userId = UserHandle.getUserId(app.uid)
    private val isChecked =
        OverridableFlow(
            flow {
                emit(packageManager.isSuperPermissionEnabled(app.packageName, userId))
            }
        )

    val isCheckedFlow = isChecked.flow

    fun setEnabled(enabled: Boolean) {
        try {
            packageManager.setSuperPermissionEnabled(app.packageName, userId, enabled)
            isChecked.override(enabled)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to change Super permissions state", e)
        }
    }

    private companion object {
        const val TAG = "SuperPermissionPref"
    }
}
