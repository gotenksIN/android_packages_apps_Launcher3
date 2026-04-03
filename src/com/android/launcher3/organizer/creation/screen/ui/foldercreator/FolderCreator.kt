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

package com.android.launcher3.organizer.creation.screen.ui.foldercreator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R

/** Composable that displays a modal bottom sheet for folder creation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderCreator(viewModel: FolderCreatorViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(R.color.materialColorSurfaceContainerLow),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            TitleSection()
            FolderList(state, viewModel)
            DuplicatesOption(state, viewModel)
            BottomActions(state, viewModel, onDismiss)
        }
    }
}

@Composable
private fun DuplicatesOption(state: FolderCreatorState, viewModel: FolderCreatorViewModel) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    start = FolderCreatorDimens.RemoveDuplicatesContainerHorizontalPadding,
                    end = FolderCreatorDimens.RemoveDuplicatesContainerHorizontalPadding,
                    top = 8.dp,
                    bottom = 4.dp,
                )
                .clip(RoundedCornerShape(FolderCreatorDimens.PreviewCornerRadius))
                .background(colorResource(R.color.materialColorSurfaceContainerHigh))
                .clickable { viewModel.toggleRemoveDuplicates() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.folder_creator_remove_duplicates),
            color = colorResource(R.color.materialColorOnSurface),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
        Checkbox(
            checked = state.removeDuplicates,
            onCheckedChange = { viewModel.toggleRemoveDuplicates() },
            colors =
                CheckboxDefaults.colors(
                    checkedColor = colorResource(R.color.materialColorPrimary),
                    uncheckedColor = colorResource(R.color.materialColorPrimary),
                    checkmarkColor = colorResource(R.color.materialColorOnPrimary),
                ),
        )
    }
}

@Composable
private fun TitleSection() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.folder_creator_title),
            color = colorResource(R.color.materialColorOnSurface),
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
        )
    }
}

@Composable
private fun FolderList(state: FolderCreatorState, viewModel: FolderCreatorViewModel) {
    FlowRow(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    start = FolderCreatorDimens.ListHorizontalPadding,
                    end = FolderCreatorDimens.ListHorizontalPadding,
                    top = FolderCreatorDimens.ListTopPadding,
                    bottom = FolderCreatorDimens.ListBottomPadding,
                ),
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(FolderCreatorDimens.ListItemSpacing),
        verticalArrangement = Arrangement.spacedBy(FolderCreatorDimens.ListItemSpacing),
    ) {
        state.topics.forEach { topicData ->
            FolderPreview(
                modifier = Modifier.weight(1f),
                topicData,
                isSelected = state.selectedTopics.contains(topicData.topic),
                onFolderClick = { viewModel.toggleSelection(topicData.topic) },
            )
        }
    }
}

@Composable
private fun FolderPreview(
    modifier: Modifier = Modifier,
    topicData: FolderTopicData,
    isSelected: Boolean,
    onFolderClick: () -> Unit,
) {
    val shape = RoundedCornerShape(FolderCreatorDimens.PreviewCornerRadius)

    Column(
        modifier =
            modifier
                .height(FolderCreatorDimens.PreviewHeight)
                .clip(shape)
                .background(colorResource(R.color.materialColorSurfaceContainerHigh))
                .then(
                    if (isSelected) {
                        Modifier.border(
                            3.dp,
                            colorResource(R.color.materialColorPrimaryFixed),
                            shape,
                        )
                    } else Modifier
                )
                .clickable { onFolderClick.invoke() }
                .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.padding(bottom = FolderCreatorDimens.ListItemSpacing),
            contentAlignment = Alignment.Center,
        ) {
            FolderItem(topicData)
        }
        Text(
            text = topicData.topic,
            color = colorResource(R.color.materialColorOnSurface),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

@Composable
private fun FolderItem(topicData: FolderTopicData) {
    Box(
        modifier =
            Modifier.size(50.dp)
                .background(colorResource(R.color.materialColorSurfaceDim), CircleShape)
    )
    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(FolderCreatorDimens.PreviewIconInnerSpacing),
        verticalArrangement = Arrangement.spacedBy(FolderCreatorDimens.PreviewIconInnerSpacing),
    ) {
        topicData.icons.take(4).forEach { bitmap ->
            Image(
                modifier = Modifier.size(20.dp).clip(CircleShape),
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun BottomActions(
    state: FolderCreatorState,
    viewModel: FolderCreatorViewModel,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CancelButton(onDismiss = onDismiss)
        AddButton(state = state, viewModel = viewModel, onDismiss = onDismiss)
    }
}

@Composable
private fun CancelButton(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    OutlinedButton(
        modifier = modifier,
        onClick = onDismiss,
        contentPadding =
            PaddingValues(
                horizontal = FolderCreatorDimens.ButtonHorizontalPadding,
                vertical = FolderCreatorDimens.ButtonVerticalPadding,
            ),
        border = BorderStroke(1.dp, colorResource(R.color.materialColorOnPrimary)),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = colorResource(R.color.materialColorPrimary)
            ),
    ) {
        Text(
            text = stringResource(R.string.folder_creator_cancel),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}

@Composable
private fun AddButton(
    modifier: Modifier = Modifier,
    state: FolderCreatorState,
    viewModel: FolderCreatorViewModel,
    onDismiss: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = {
            viewModel.generateFolders(state.selectedTopics.toList())
            onDismiss.invoke()
        },
        enabled = state.selectedTopics.isNotEmpty(),
        contentPadding =
            PaddingValues(
                horizontal = FolderCreatorDimens.ButtonHorizontalPadding,
                vertical = FolderCreatorDimens.ButtonVerticalPadding,
            ),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.materialColorPrimary),
                contentColor = colorResource(R.color.materialColorOnPrimary),
            ),
    ) {
        Text(
            text = stringResource(R.string.folder_creator_add),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}

private object FolderCreatorDimens {
    // Folder list
    val ListItemSpacing = 8.dp
    val ListTopPadding = 16.dp
    val ListBottomPadding = 0.dp
    val ListHorizontalPadding = 24.dp

    // Folder preview
    val PreviewHeight = 114.dp
    val PreviewCornerRadius = 24.dp
    val PreviewIconInnerSpacing = 5.dp

    // Remove duplicates checkbox
    val RemoveDuplicatesContainerHorizontalPadding = 24.dp

    // Bottom bar buttons
    val ButtonHorizontalPadding = 16.dp
    val ButtonVerticalPadding = 10.dp
}
