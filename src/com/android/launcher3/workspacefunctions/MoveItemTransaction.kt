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

import com.android.launcher3.appfunctions.workspace.MoveItemParamsSpec
import com.android.launcher3.appfunctions.workspace.WorkspaceSpec
import com.android.launcher3.appfunctions.workspace.WorkspaceTransaction
import com.android.launcher3.workspacefunctions.translators.TranslatorRegistry
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/** Concrete implementation of a [WorkspaceTransaction] for moving an item. */
class MoveItemTransaction
@AssistedInject
constructor(
    @Assisted private val params: MoveItemParamsSpec,
    private val workspaceProvider: LauncherWorkspaceProvider,
    private val translators: TranslatorRegistry,
) : WorkspaceTransaction {
    override suspend fun execute(): WorkspaceSpec {
        // TODO b/457458301: implement
        val workspace = workspaceProvider.getWorkspace()
        return translators.translate(workspace)
    }

    /** Factory for creating instances of [MoveItemTransaction]. */
    @AssistedFactory
    interface Factory {
        fun create(params: MoveItemParamsSpec): MoveItemTransaction
    }
}
