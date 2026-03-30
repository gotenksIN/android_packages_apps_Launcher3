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
package com.android.launcher3.workspacefunctions

import android.util.SparseArray
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.appfunctions.workspace.ItemSelectorSpec
import com.android.launcher3.appfunctions.workspace.RemoveItemParamsSpec
import com.android.launcher3.appfunctions.workspace.WorkspaceSpec
import com.android.launcher3.appfunctions.workspace.WorkspaceTypeTranslator
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.IModelWriter
import com.android.launcher3.model.ModelWriterFactory
import com.android.launcher3.model.TransactionContext
import com.android.launcher3.model.WorkspaceItemSpaceFinder
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.model.data.WorkspaceChangeEvent
import com.android.launcher3.model.data.WorkspaceData
import com.android.launcher3.model.data.WorkspaceData.ImmutableWorkspaceData
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.model.repository.HomeScreenRepository
import com.android.launcher3.model.testing.FakeModelWriter
import com.android.launcher3.model.testing.WriterAction
import com.android.launcher3.organizer.OrganizerTransactionContext
import com.android.launcher3.workspacefunctions.translators.TranslatorRegistry
import com.google.common.truth.Truth.assertThat
import javax.inject.Provider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class RemoveItemTransactionTest {

    private val homeScreenRepository = HomeScreenRepository()
    private val workspaceProvider = LauncherWorkspaceProvider(homeScreenRepository)
    private val fakeModelWriter = FakeModelWriter()

    private val modelWriterFactory =
        object : ModelWriterFactory {
            override fun create(
                verifyChanges: Boolean,
                cellPosMapper: com.android.launcher3.celllayout.CellPosMapper,
                modificationSource: BgDataModel.ModificationSource,
                owner: BgDataModel.Callbacks?,
                modelExecutor: java.util.concurrent.Executor,
                uiExecutor: java.util.concurrent.Executor?,
            ): IModelWriter = fakeModelWriter
        }

    private val workspaceTypeTranslators =
        mutableMapOf<Class<*>, Provider<WorkspaceTypeTranslator<*>>>()
    private val translators =
        TranslatorRegistry(
            workspaceItemTranslators = emptyMap(),
            hotseatItemTranslators = emptyMap(),
            appInFolderTranslators = emptyMap(),
            workspaceTypeTranslators = workspaceTypeTranslators,
            unplacedAppTypeTranslators = emptyMap(),
            unplacedWidgetTypeTranslators = emptyMap(),
        )

    private val organizerTransactionContextFactory =
        object : OrganizerTransactionContext.Factory {
            override fun create(delegate: TransactionContext): OrganizerTransactionContext {
                return OrganizerTransactionContext(
                    delegate,
                    mock<BgDataModel>(),
                    mock<WorkspaceItemSpaceFinder>(),
                    homeScreenRepository,
                )
            }
        }

    @Test
    fun execute_removesMatchingItems() = runTest {
        val item1 =
            WorkspaceItemInfo().apply {
                id = 1
                title = "App 1"
                container = Favorites.CONTAINER_DESKTOP
                screenId = 1
                cellX = 0
                cellY = 0
            }
        val item2 =
            WorkspaceItemInfo().apply {
                id = 2
                title = "App 2"
                container = Favorites.CONTAINER_DESKTOP
                screenId = 1
                cellX = 1
                cellY = 1
            }
        val workspaceData = createWorkspaceData(item1, item2)
        homeScreenRepository.dispatchWorkspaceDataChange(
            workspaceData,
            WorkspaceChangeEvent.FullRefresh("Initial Load"),
        )

        val params =
            RemoveItemParamsSpec(
                item =
                    ItemSelectorSpec(
                        label = "App 1",
                        screenIndex = null,
                        x = null,
                        y = null,
                        hotseatRank = null,
                        packageName = null,
                        className = null,
                    )
            )
        val transaction =
            RemoveItemTransaction(
                params = params,
                modelWriterFactory = modelWriterFactory,
                homeScreenRepository = homeScreenRepository,
                workspaceProvider = workspaceProvider,
                translators = translators,
                organizerTransactionContextFactory = organizerTransactionContextFactory,
            )

        val workspaceSpec =
            WorkspaceSpec(
                emptyList(),
                com.android.launcher3.appfunctions.workspace.HotseatSpec(emptyList()),
                null,
                null,
            )
        val mockTranslator =
            object : WorkspaceTypeTranslator<WorkspaceData> {
                override fun toSpec(info: WorkspaceData): WorkspaceSpec = workspaceSpec
            }
        workspaceTypeTranslators[ImmutableWorkspaceData::class.java] = Provider { mockTranslator }

        val result = transaction.execute()

        assertThat(result).isEqualTo(workspaceSpec)
        // Verify that delete action was recorded for item1
        val deleteActions = fakeModelWriter.actions.filterIsInstance<WriterAction.DeleteItem>()
        assertThat(deleteActions.any { it.item.id == item1.id }).isTrue()
        assertThat(deleteActions.any { it.item.id == item2.id }).isFalse()
    }

    @Test
    fun execute_noMatchingItems_removesNothing() = runTest {
        val item1 =
            WorkspaceItemInfo().apply {
                id = 1
                title = "App 1"
                container = Favorites.CONTAINER_DESKTOP
                screenId = 1
                cellX = 0
                cellY = 0
            }
        val workspaceData = createWorkspaceData(item1)
        homeScreenRepository.dispatchWorkspaceDataChange(
            workspaceData,
            WorkspaceChangeEvent.FullRefresh("Initial Load"),
        )

        val params =
            RemoveItemParamsSpec(
                item =
                    ItemSelectorSpec(
                        label = "Non-existent App",
                        screenIndex = null,
                        x = null,
                        y = null,
                        hotseatRank = null,
                        packageName = null,
                        className = null,
                    )
            )
        val transaction =
            RemoveItemTransaction(
                params = params,
                modelWriterFactory = modelWriterFactory,
                homeScreenRepository = homeScreenRepository,
                workspaceProvider = workspaceProvider,
                translators = translators,
                organizerTransactionContextFactory = organizerTransactionContextFactory,
            )

        val workspaceSpec =
            WorkspaceSpec(
                emptyList(),
                com.android.launcher3.appfunctions.workspace.HotseatSpec(emptyList()),
                null,
                null,
            )
        val mockTranslator =
            object : WorkspaceTypeTranslator<WorkspaceData> {
                override fun toSpec(info: WorkspaceData): WorkspaceSpec = workspaceSpec
            }
        workspaceTypeTranslators[ImmutableWorkspaceData::class.java] = Provider { mockTranslator }

        transaction.execute()

        val deleteActions = fakeModelWriter.actions.filterIsInstance<WriterAction.DeleteItem>()
        assertThat(deleteActions).isEmpty()
    }

    private fun createWorkspaceData(vararg items: ItemInfo): WorkspaceData {
        val sparseArray = SparseArray<ItemInfo>()
        items.forEach { sparseArray.put(it.id, it) }
        return ImmutableWorkspaceData(1, 0, sparseArray)
    }
}
