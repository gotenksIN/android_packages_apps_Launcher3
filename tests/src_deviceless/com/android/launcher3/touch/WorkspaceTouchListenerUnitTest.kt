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

package com.android.launcher3.touch

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.Workspace
import com.android.launcher3.dragndrop.DragLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.LooperMode

@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(AndroidJUnit4::class)
class WorkspaceTouchListenerUnitTest {
    @Mock private lateinit var mMockLauncher: Launcher
    @Mock private lateinit var mMockWorkspace: Workspace<*>
    @Mock private lateinit var mDragLayer: DragLayer

    private lateinit var mContext: Context
    private lateinit var mWorkspaceTouchListener: WorkspaceTouchListener

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mContext = ApplicationProvider.getApplicationContext()

        // Provide real Resources for ViewConfiguration initialization
        whenever(mMockLauncher.resources).thenReturn(mContext.resources)
        whenever(mMockLauncher.isInState(LauncherState.NORMAL)).thenReturn(true)
        whenever(mMockLauncher.deviceProfile)
            .thenReturn(InvariantDeviceProfile.INSTANCE[mContext].getDeviceProfile(mContext))

        // Ensure a drag layer can be used for simulated touch events.
        whenever(mMockLauncher.dragLayer).thenReturn(mDragLayer)
        whenever(mDragLayer.width).thenReturn(200)
        whenever(mDragLayer.height).thenReturn(200)

        mWorkspaceTouchListener = WorkspaceTouchListener(mMockLauncher, mMockWorkspace)
        mMockLauncher.onTopResumedActivityChanged(false)
    }

    @Test
    fun onWorkspaceTouch_whenHomeBehindDesktop_launchesHomeIntent() {
        whenever(mMockLauncher.shouldShowHomeBehindDesktop()).thenReturn(true)

        // Simulate a tap event in the workspace.
        val downTime = SystemClock.uptimeMillis()
        val downEvent =
            MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 100f, 100f, 0)
        mWorkspaceTouchListener.onTouch(null, downEvent)
        val upEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_UP, 100f, 100f, 0)
        mWorkspaceTouchListener.onTouch(null, upEvent)

        // Verify startActivity was called with the correct Intent
        val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        verify(mMockLauncher).startActivity(intentCaptor.capture())

        val capturedIntent = intentCaptor.value
        assertEquals(
            "Intent action should be ACTION_MAIN",
            Intent.ACTION_MAIN,
            capturedIntent.action,
        )
        assertTrue(
            "Intent should have CATEGORY_HOME",
            capturedIntent.hasCategory(Intent.CATEGORY_HOME),
        )
        assertTrue(
            "Intent should have FLAG_ACTIVITY_NEW_TASK",
            (capturedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) ==
                Intent.FLAG_ACTIVITY_NEW_TASK,
        )
    }

    @Test
    fun onWorkspaceTouch_doesNotLaunchHomeIntent() {
        whenever(mMockLauncher.shouldShowHomeBehindDesktop()).thenReturn(false)

        // Simulate a tap event in the workspace.
        val downTime = SystemClock.uptimeMillis()
        val downEvent =
            MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 100f, 100f, 0)
        mWorkspaceTouchListener.onTouch(null, downEvent)
        val upEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_UP, 100f, 100f, 0)
        mWorkspaceTouchListener.onTouch(null, upEvent)

        // Verify that no Intent is called.
        verify(mMockLauncher, never()).startActivity(any(Intent::class.java))
    }
}
