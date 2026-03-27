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

import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.appfunctions.workspace.RemoveItemParamsSpec
import com.android.launcher3.appfunctions.workspace.WorkspaceSpec
import com.android.launcher3.appfunctions.workspace.WorkspaceTransaction
import com.android.launcher3.appfunctions.workspace.validators.matchesItemSpec
import com.android.launcher3.celllayout.CellPosMapper
import com.android.launcher3.model.BgDataModel.ModificationSource.ModelTask
import com.android.launcher3.model.ModelWriterFactory
import com.android.launcher3.model.repository.HomeScreenRepository
import com.android.launcher3.model.scheduleTransactionSuspending
import com.android.launcher3.organizer.OrganizerTransactionContext
import com.android.launcher3.workspacefunctions.translators.TranslatorRegistry
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * Concrete implementation of a [WorkspaceTransaction] for removing an item.
 *
 * This class uses the [ModelWriterFactory] to create a [ModelWriter] and schedules a transaction to
 * delete items from the database based on the provided [RemoveItemParamsSpec].
 */
class RemoveItemTransaction
@AssistedInject
constructor(
    @Assisted private val params: RemoveItemParamsSpec,
    private val modelWriterFactory: ModelWriterFactory,
    private val homeScreenRepository: HomeScreenRepository,
    private val workspaceProvider: LauncherWorkspaceProvider,
    private val translators: TranslatorRegistry,
    private val organizerTransactionContextFactory: OrganizerTransactionContext.Factory,
) : WorkspaceTransaction {

    override suspend fun execute(): WorkspaceSpec {
        val modelWriter =
            modelWriterFactory.create(
                verifyChanges = true,
                cellPosMapper = CellPosMapper.DEFAULT,
                modificationSource = ModelTask,
                owner = null,
            )

        modelWriter.scheduleTransactionSuspending { context ->
            val organizerContext = organizerTransactionContextFactory.create(context)
            val itemsToDelete =
                homeScreenRepository.workspaceState.value.filter { item ->
                    params.item.matchesItemSpec(
                        packageName = item.targetPackage,
                        className = item.targetComponent?.className,
                        labels = listOf(item.title, item.appTitle),
                        screenIndex =
                            if (item.container == Favorites.CONTAINER_DESKTOP) item.screenId
                            else null,
                        x = item.cellX,
                        y = item.cellY,
                        hotseatRank =
                            if (item.container == Favorites.CONTAINER_HOTSEAT) item.screenId
                            else null,
                        isInHotseat = item.container == Favorites.CONTAINER_HOTSEAT,
                        isInDesktop = item.container == Favorites.CONTAINER_DESKTOP,
                    )
                }
            organizerContext.deleteItemsFromDatabase(itemsToDelete, "AppFunction: RemoveItem")
        }

        val workspace = workspaceProvider.getWorkspace()
        return translators.translate(workspace)
    }

    /** Factory for creating instances of [RemoveItemTransaction]. */
    @AssistedFactory
    interface Factory {
        fun create(params: RemoveItemParamsSpec): RemoveItemTransaction
    }
}
