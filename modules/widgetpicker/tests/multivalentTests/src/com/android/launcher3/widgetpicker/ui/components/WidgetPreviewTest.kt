/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.launcher3.widgetpicker.ui.components

import android.platform.test.annotations.EnableFlags
import android.platform.test.flag.junit.SetFlagsRule
import android.platform.test.rule.DeniedDevices
import android.platform.test.rule.DeviceProduct
import android.platform.test.rule.LimitDevicesRule
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.launcher3.Flags.FLAG_ENABLE_CURSOR_DRIVEN_WORKFLOWS
import com.android.launcher3.widgetpicker.TestUtils
import com.android.launcher3.widgetpicker.TestUtils.PERSONAL_TEST_APPS
import com.android.launcher3.widgetpicker.shared.model.WidgetSizeInfo
import com.android.launcher3.widgetpicker.ui.WidgetInteractionInfo
import com.android.launcher3.widgetpicker.ui.WidgetInteractionSource
import com.android.launcher3.widgetpicker.ui.theme.WidgetPickerTheme
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@DeniedDevices(denied = [DeviceProduct.ROBOLECTRIC])
class WidgetPreviewTest {
    @get:Rule val limitDevicesRule = LimitDevicesRule()
    @get:Rule val setFlagsRule = SetFlagsRule()
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @EnableFlags(FLAG_ENABLE_CURSOR_DRIVEN_WORKFLOWS)
    fun dragPreview_withTouch_afterLongPress_invokesDragCallback() {
        var dragInvoked = false
        composeTestRule.setContent {
            WidgetPickerTheme {
                WidgetPreview(
                    id = WIDGET_ONE.id,
                    sizeInfo = SIZE_INFO,
                    preview = PREVIEW,
                    widgetInfo = WIDGET_ONE.widgetInfo,
                    modifier = Modifier.testTag(PREVIEW_TEST_TAG),
                    showDragShadow = false,
                    widgetInteractionSource = WidgetInteractionSource.BROWSE,
                    onWidgetInteraction = {
                        if (it is WidgetInteractionInfo.WidgetDragInfo) dragInvoked = true
                    },
                    onClick = {},
                    onHoverChange = {},
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(PREVIEW_TEST_TAG).performTouchInput {
            down(center)
            // Wait for long press timeout (usually 500ms, using 600ms to be safe)
            advanceEventTime(600)
            moveBy(Offset(10f, 10f))
            up()
        }

        composeTestRule.waitForIdle()
        assertThat(dragInvoked).isTrue()
    }

    @Test
    @EnableFlags(FLAG_ENABLE_CURSOR_DRIVEN_WORKFLOWS)
    fun dragPreview_withTouch_withoutLongPress_doesNotInvokeDragCallback() {
        var dragInvoked = false
        composeTestRule.setContent {
            WidgetPickerTheme {
                WidgetPreview(
                    id = WIDGET_ONE.id,
                    sizeInfo = SIZE_INFO,
                    preview = PREVIEW,
                    widgetInfo = WIDGET_ONE.widgetInfo,
                    modifier = Modifier.testTag(PREVIEW_TEST_TAG),
                    showDragShadow = false,
                    widgetInteractionSource = WidgetInteractionSource.BROWSE,
                    onWidgetInteraction = {
                        if (it is WidgetInteractionInfo.WidgetDragInfo) dragInvoked = true
                    },
                    onClick = {},
                    onHoverChange = {},
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(PREVIEW_TEST_TAG).performTouchInput {
            swipe(start = center, end = center + Offset(10f, 10f), durationMillis = 100)
        }

        composeTestRule.waitForIdle()
        assertThat(dragInvoked).isFalse()
    }

    @Test
    @EnableFlags(FLAG_ENABLE_CURSOR_DRIVEN_WORKFLOWS)
    fun dragPreview_withMouse_invokesDragCallbackImmediately() {
        var dragInvoked = false
        composeTestRule.setContent {
            WidgetPickerTheme {
                WidgetPreview(
                    id = WIDGET_ONE.id,
                    sizeInfo = SIZE_INFO,
                    preview = PREVIEW,
                    widgetInfo = WIDGET_ONE.widgetInfo,
                    modifier = Modifier.testTag(PREVIEW_TEST_TAG),
                    showDragShadow = false,
                    widgetInteractionSource = WidgetInteractionSource.BROWSE,
                    onWidgetInteraction = {
                        if (it is WidgetInteractionInfo.WidgetDragInfo) dragInvoked = true
                    },
                    onClick = {},
                    onHoverChange = {},
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(PREVIEW_TEST_TAG).performMouseInput {
            press()
            // Mouse should trigger drag immediately, no need to advance event time significantly
            moveTo(center + Offset(10f, 10f))
            release()
        }

        composeTestRule.waitForIdle()
        assertThat(dragInvoked).isTrue()
    }

    companion object {
        private val WIDGET_ONE = PERSONAL_TEST_APPS[0].widgets[0]
        private val SIZE_INFO =
            WidgetSizeInfo(
                spanX = 1,
                spanY = 1,
                widthPx = 200,
                heightPx = 200,
                containerSpanX = 1,
                containerSpanY = 1,
                containerWidthPx = 200,
                containerHeightPx = 200,
            )
        private val PREVIEW = TestUtils.createBitmapPreview()
        private const val PREVIEW_TEST_TAG = "test_preview_tag"
    }
}
