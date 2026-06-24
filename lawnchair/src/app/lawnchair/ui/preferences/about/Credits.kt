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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.lawnchair.ui.preferences.LocalIsExpandedScreen
import app.lawnchair.ui.preferences.components.layout.PreferenceLayoutLazyColumn
import app.lawnchair.ui.preferences.components.layout.preferenceGroupItems
import com.android.launcher3.R

@Composable
fun Credits(
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PreferenceLayoutLazyColumn(
        label = stringResource(id = R.string.credits),
        modifier = modifier,
        backArrowVisible = !LocalIsExpandedScreen.current,
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                uiState.topLinks.forEach { link ->
                    LawnchairLink(
                        iconResId = link.iconResId,
                        label = stringResource(id = link.labelResId),
                        modifier = Modifier.weight(weight = 1f),
                        url = link.url,
                    )
                }
            }
        }
        preferenceGroupItems(
            items = uiState.coreTeam,
            key = { _, it -> it.name },
            isFirstChild = false,
            heading = { stringResource(id = R.string.product) },
        ) { _, it ->
            ContributorRow(
                member = it,
            )
        }
        preferenceGroupItems(
            items = uiState.supportAndPr,
            key = { _, it -> it.name },
            isFirstChild = false,
            heading = { stringResource(id = R.string.support_and_pr) },
        ) { _, it ->
            ContributorRow(
                member = it,
            )
        }
        preferenceGroupItems(
            items = uiState.bottomLinks,
            key = { _, it -> it.labelResId },
            isFirstChild = false,
            heading = { stringResource(id = R.string.community) },
        ) { _, it ->
            HorizontalLawnchairLink(
                iconResId = it.iconResId,
                label = stringResource(id = it.labelResId),
                url = it.url,
            )
        }
    }
}
