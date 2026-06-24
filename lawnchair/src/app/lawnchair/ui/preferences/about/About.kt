/*
 * Copyright 2022, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair.ui.preferences.about

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.lawnchair.preferences.getAdapter
import app.lawnchair.preferences2.preferenceManager2
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.NavigationActionPreference
import app.lawnchair.ui.preferences.components.controls.ClickablePreference
import app.lawnchair.ui.preferences.components.controls.SwitchPreference
import app.lawnchair.ui.preferences.components.layout.PreferenceDivider
import app.lawnchair.ui.preferences.components.layout.PreferenceGroupHeading
import app.lawnchair.ui.preferences.components.layout.PreferenceGroupItem
import app.lawnchair.ui.preferences.components.layout.PreferenceLayoutLazyColumn
import app.lawnchair.ui.preferences.navigation.AboutCredits
import app.lawnchair.ui.preferences.navigation.AboutLicenses
import com.android.launcher3.BuildConfig
import com.android.launcher3.R

private const val ADVANCED_MODE_TAP_THRESHOLD = 7

@Composable
fun About(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs2 = preferenceManager2()
    val advancedModeUnlockedAdapter = prefs2.advancedModeUnlocked.getAdapter()
    val advancedModeAdapter = prefs2.advancedMode.getAdapter()
    val advancedModeUnlocked = advancedModeUnlockedAdapter.state.value
    var logoTapCount by remember { mutableIntStateOf(0) }
    var countdownToast by remember { mutableStateOf<Toast?>(null) }

    PreferenceLayoutLazyColumn(
        label = stringResource(id = R.string.about_label),
        modifier = modifier,
        backArrowVisible = !LocalIsExpandedScreen.current,
    ) {
        item {
            Spacer(Modifier.padding(top = 8.dp))
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (advancedModeUnlocked) {
                                countdownToast?.cancel()
                                countdownToast = Toast.makeText(
                                    context,
                                    R.string.advanced_mode_already_enabled_toast,
                                    Toast.LENGTH_SHORT,
                                ).also { it.show() }
                                return@clickable
                            }
                            logoTapCount++
                            val remaining = ADVANCED_MODE_TAP_THRESHOLD - logoTapCount
                            countdownToast?.cancel()
                            when {
                                remaining <= 0 -> {
                                    logoTapCount = 0
                                    advancedModeUnlockedAdapter.onChange(true)
                                    advancedModeAdapter.onChange(true)
                                    countdownToast = Toast.makeText(
                                        context,
                                        R.string.advanced_mode_enabled_toast,
                                        Toast.LENGTH_SHORT,
                                    ).also { it.show() }
                                }
                                // Start counting down once only a few taps remain,
                                // mirroring Android's "steps away from being a developer" prompt.
                                remaining <= ADVANCED_MODE_TAP_THRESHOLD - 3 -> {
                                    countdownToast = Toast.makeText(
                                        context,
                                        context.resources.getQuantityString(
                                            R.plurals.advanced_mode_steps_away_toast,
                                            remaining,
                                            remaining,
                                        ),
                                        Toast.LENGTH_SHORT,
                                    ).also { it.show() }
                                }
                            }
                        },
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Text(
                text = stringResource(id = R.string.derived_app_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = BuildConfig.VERSION_DISPLAY_NAME,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                val projectUrl = "https://github.com/cposth-cnx/cpos-home"
                                context.startActivity(Intent(Intent.ACTION_VIEW, projectUrl.toUri()))
                            },
                        ),
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Text(
                text = stringResource(id = R.string.about_thanks_lawnchair),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        if (advancedModeUnlocked) {
            item {
                PreferenceGroupItem(
                    cutTop = false,
                    cutBottom = true,
                ) {
                    SwitchPreference(
                        checked = advancedModeAdapter.state.value,
                        onCheckedChange = { enabled ->
                            advancedModeAdapter.onChange(enabled)
                            // Turning Advanced Mode off also re-locks it, hiding the toggle.
                            // It can be unlocked again by tapping the logo 7 times.
                            if (!enabled) {
                                advancedModeUnlockedAdapter.onChange(false)
                            }
                        },
                        label = stringResource(id = R.string.advanced_mode_label),
                        description = stringResource(id = R.string.advanced_mode_description),
                    )
                }
            }
        }
        item {
            PreferenceGroupItem(
                cutTop = advancedModeUnlocked,
                cutBottom = false,
            ) {
                if (advancedModeUnlocked) {
                    PreferenceDivider()
                }
                NavigationActionPreference(
                    label = stringResource(id = R.string.credits),
                    destination = AboutCredits,
                )
            }
        }
        item {
            PreferenceGroupHeading(
                stringResource(R.string.legal),
            )
        }
        item {
            PreferenceGroupItem(
                cutTop = false,
                cutBottom = true,
            ) {
                NavigationActionPreference(
                    label = stringResource(id = R.string.acknowledgements),
                    destination = AboutLicenses,
                )
            }
        }
        item {
            PreferenceGroupItem(
                cutTop = true,
                cutBottom = false,
            ) {
                PreferenceDivider()
                ClickablePreference(
                    label = stringResource(id = R.string.privacy_policy),
                    onClick = {
                        val webpage = PRIVACY_POLICY.toUri()
                        val intent = Intent(Intent.ACTION_VIEW, webpage)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    },
                )
            }
        }
    }
}

private const val PRIVACY_POLICY = "https://lawnchair.app/privacy_policy"
