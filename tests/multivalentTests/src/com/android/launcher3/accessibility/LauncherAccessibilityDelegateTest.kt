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

package com.android.launcher3.accessibility

import android.content.ClipDescription.MIMETYPE_UNKNOWN
import android.content.ClipboardManager
import android.platform.test.annotations.DisableFlags
import android.platform.test.annotations.EnableFlags
import android.platform.test.flag.junit.SetFlagsRule
import android.provider.DocumentsContract.Document.MIME_TYPE_DIR
import android.view.View
import androidx.test.filters.SmallTest
import com.android.launcher3.DropTargetBar
import com.android.launcher3.Flags.FLAG_ENABLE_HOME_SCREEN_FILES_COPY_PASTE
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherSettings
import com.android.launcher3.PendingAddItemInfo
import com.android.launcher3.accessibility.LauncherAccessibilityDelegate.ADD_TO_WORKSPACE
import com.android.launcher3.accessibility.LauncherAccessibilityDelegate.COPY
import com.android.launcher3.homescreenfiles.HomeScreenFile
import com.android.launcher3.homescreenfiles.HomeScreenFilesUtils
import com.android.launcher3.homescreenfiles.homeScreenFile
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.ItemInfoWithIcon
import com.android.launcher3.model.data.ItemInfoWithIcon.FLAG_NOT_PINNABLE
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.util.LauncherMultivalentJUnit
import com.android.launcher3.util.SandboxApplication
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

/** Tests for {@link LauncherAccessibilityDelegate}. */
@SmallTest
@RunWith(LauncherMultivalentJUnit::class)
class LauncherAccessibilityDelegateTest {

    @get:Rule val flags = SetFlagsRule()
    @get:Rule val app = SandboxApplication()
    @get:Rule val mockito = MockitoJUnit.rule()

    @Mock private lateinit var dropTargetBar: DropTargetBar
    @Mock private lateinit var host: View
    @Mock private lateinit var launcher: Launcher

    private lateinit var delegate: LauncherAccessibilityDelegate

    @Before
    fun setUp() {
        whenever(dropTargetBar.dropTargets).thenReturn(emptyArray())
        whenever(launcher.dropTargetBar).thenReturn(dropTargetBar)
        delegate = LauncherAccessibilityDelegate(launcher)
    }

    @Test
    fun testSupportsAddToWorkspaceWithAppInfo() {
        testSupportsAddToWorkspace(AppInfo())
    }

    @Test
    fun testSupportsAddToWorkspaceWithPendingAddItemInfo() {
        testSupportsAddToWorkspace(PendingAddItemInfo())
    }

    @Test
    fun testSupportsAddToWorkspaceWithWorkspaceItemInfo() {
        testSupportsAddToWorkspace(WorkspaceItemInfo())
    }

    private fun testSupportsAddToWorkspace(itemInfo: ItemInfoWithIcon) {
        testSupportsAddToWorkspace(
            itemInfo.apply {
                container = LauncherSettings.Favorites.CONTAINER_ALL_APPS
                runtimeStatusFlags = 0
            },
            expectSupport = true,
        )
        testSupportsAddToWorkspace(
            itemInfo.apply {
                container = LauncherSettings.Favorites.CONTAINER_ALL_APPS
                runtimeStatusFlags = FLAG_NOT_PINNABLE
            },
            expectSupport = false,
        )
        testSupportsAddToWorkspace(
            itemInfo.apply {
                container = LauncherSettings.Favorites.CONTAINER_DESKTOP
                runtimeStatusFlags = 0
            },
            expectSupport = false,
        )
        testSupportsAddToWorkspace(
            itemInfo.apply {
                container = LauncherSettings.Favorites.CONTAINER_DESKTOP
                runtimeStatusFlags = FLAG_NOT_PINNABLE
            },
            expectSupport = false,
        )
    }

    private fun testSupportsAddToWorkspace(itemInfo: ItemInfo, expectSupport: Boolean) {
        val actions = mutableListOf<BaseAccessibilityDelegate<Launcher>.LauncherAction>()
        delegate.getSupportedActions(host, itemInfo, actions)
        assertEquals(
            expectSupport,
            actions.any { action -> action.accessibilityAction.id == ADD_TO_WORKSPACE },
        )
    }

    @Test
    @DisableFlags(FLAG_ENABLE_HOME_SCREEN_FILES_COPY_PASTE)
    fun testSupportsCopyWithFileSystemFileAndFlagDisabled() {
        testSupportsCopyWithFileSystemItem(isDirectory = false, expectSupport = false)
    }

    @Test
    @EnableFlags(FLAG_ENABLE_HOME_SCREEN_FILES_COPY_PASTE)
    fun testSupportsCopyWithFileSystemFileAndFlagEnabled() {
        testSupportsCopyWithFileSystemItem(isDirectory = false, expectSupport = true)
    }

    @Test
    @DisableFlags(FLAG_ENABLE_HOME_SCREEN_FILES_COPY_PASTE)
    fun testSupportsCopyWithFileSystemFolderAndFlagDisabled() {
        testSupportsCopyWithFileSystemItem(isDirectory = true, expectSupport = false)
    }

    @Test
    @EnableFlags(FLAG_ENABLE_HOME_SCREEN_FILES_COPY_PASTE)
    fun testSupportsCopyWithFileSystemFolderAndFlagEnabled() {
        testSupportsCopyWithFileSystemItem(isDirectory = true, expectSupport = true)
    }

    private fun testSupportsCopyWithFileSystemItem(isDirectory: Boolean, expectSupport: Boolean) {
        val file =
            HomeScreenFile(
                displayName = if (isDirectory) "Folder" else "File",
                mimeType = if (isDirectory) MIME_TYPE_DIR else MIMETYPE_UNKNOWN,
                isDirectory = isDirectory,
                uri = mock(),
                user = mock(),
            )

        testSupportsCopy(
            itemInfo =
                WorkspaceItemInfo().apply {
                    itemType = HomeScreenFilesUtils.buildItemType(file)
                    intent = HomeScreenFilesUtils.buildLaunchIntent(file.uri, file)
                },
            expectSupport = expectSupport,
        )
    }

    @Test
    @DisableFlags(FLAG_ENABLE_HOME_SCREEN_FILES_COPY_PASTE)
    fun testSupportsCopyWithNonFileSystemItemAndFlagDisabled() {
        testSupportsCopy(itemInfo = ItemInfo(), expectSupport = false)
    }

    @Test
    @EnableFlags(FLAG_ENABLE_HOME_SCREEN_FILES_COPY_PASTE)
    fun testSupportsCopyWithNonFileSystemItemAndFlagEnabled() {
        testSupportsCopy(itemInfo = ItemInfo(), expectSupport = false)
    }

    private fun testSupportsCopy(itemInfo: ItemInfo, expectSupport: Boolean) {
        // Verify action support.
        val actions = mutableListOf<BaseAccessibilityDelegate<Launcher>.LauncherAction>()
        delegate.getSupportedActions(host, itemInfo, actions)
        assertEquals(expectSupport, actions.any { action -> action.accessibilityAction.id == COPY })

        // Mock clipboard.
        val clipboardManager = mock<ClipboardManager>()
        whenever(launcher.getSystemService(ClipboardManager::class.java))
            .thenReturn(clipboardManager)

        // Verify action performance.
        assertEquals(
            expectSupport,
            delegate.performAction(host, itemInfo, COPY, /* fromKeyboard= */ true),
        )

        // Verify clipboard state.
        if (expectSupport) {
            val file = itemInfo.homeScreenFile!!
            val mimeType = if (file.isDirectory) MIME_TYPE_DIR else file.mimeType
            verify(clipboardManager)
                .setPrimaryClip(
                    argThat {
                        description.mimeTypeCount == 1 &&
                            description.getMimeType(0) == mimeType &&
                            itemCount == 1 &&
                            getItemAt(0).uri == file.uri
                    }
                )
        }
        verifyNoMoreInteractions(clipboardManager)
    }
}
