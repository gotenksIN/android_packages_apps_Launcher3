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

package com.android.launcher3.appfunctions.workspace

import com.android.launcher3.appfunctions.workspace.WorkspaceAppFunctions.Proof
import com.android.launcher3.appfunctions.workspace.validators.MoveItemValidator
import com.android.launcher3.appfunctions.workspace.validators.RemoveItemValidator
import com.android.launcher3.appfunctions.workspace.validators.SelectorValidator
import com.android.launcher3.appfunctions.workspace.validators.ValidationResult

/** Manages workspace mutations by validating requests before execution. */
class WorkspaceMutationManager(
    private val repository: WorkspaceRepository,
    private val transactionFactory: WorkspaceTransactionFactory,
) {

    /**
     * Executes a remove item operation after validation.
     *
     * @param params Parameters for the remove operation.
     * @return [WorkspaceUpdateResult] indicating success or failure.
     */
    suspend fun removeItem(params: RemoveItemParamsSpec): WorkspaceUpdateResult {
        return executeMutation(
            validator = RemoveItemValidator(params, repository),
            executeTransaction = {
                transactionFactory.createRemoveItemTransaction(params).execute()
            },
            successMessage = "Item removed",
            successChanges = "Removed item ${params.item}",
            proof = Proof.REMOVE_ITEM_PROOF,
        )
    }

    suspend fun moveItem(params: MoveItemParamsSpec): WorkspaceUpdateResult {
        return executeMutation(
            validator = MoveItemValidator(params, repository),
            executeTransaction = { transactionFactory.createMoveItemTransaction(params).execute() },
            successMessage = "Item moved",
            successChanges = "Moved item ${params.source} to ${params.destination}",
            proof = Proof.MOVE_ITEM_PROOF,
        )
    }

    private suspend fun executeMutation(
        validator: SelectorValidator,
        executeTransaction: suspend () -> Unit,
        successMessage: String,
        successChanges: String,
        proof: Proof,
    ): WorkspaceUpdateResult {
        return when (val validationResult = validator.validate()) {
            is ValidationResult.Valid -> {
                executeTransaction()

                // TODO b/494314201: add diffing logic
                // TODO b/493993708: replace any dummy data with real implementation
                WorkspaceUpdateResult(
                    success = true,
                    message = successMessage,
                    changes = successChanges,
                    errorCode = null,
                    resolvedItemIdentifier = null,
                    resolutionDetails = null,
                    proof = proof,
                )
            }

            is ValidationResult.Invalid -> {
                WorkspaceUpdateResult(
                    success = false,
                    message = validationResult.message,
                    changes = null,
                    errorCode = validationResult.errorCode,
                    resolvedItemIdentifier = null,
                    resolutionDetails = validationResult.resolutionDetails,
                    proof = Proof.NO_PROOF,
                )
            }
        }
    }
}
