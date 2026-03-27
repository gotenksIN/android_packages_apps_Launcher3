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

package com.android.launcher3.appfunctions.workspace.validators

import com.android.launcher3.appfunctions.workspace.AppInFolderSpec
import com.android.launcher3.appfunctions.workspace.ErrorCode
import com.android.launcher3.appfunctions.workspace.HotseatItemSpec
import com.android.launcher3.appfunctions.workspace.ItemSelectorSpec
import com.android.launcher3.appfunctions.workspace.WorkspaceItemSpec

/**
 * Validates that an [ItemSelectorSpec] is correctly specified.
 *
 * @property selector The selector to validate.
 */
class ItemSelectorValidator(private val selector: ItemSelectorSpec) : SelectorValidator {
    override suspend fun validate(): ValidationResult {
        return if (selector.isComplete()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                message = "Invalid item selector",
                errorCode = ErrorCode(ErrorCode.INVALID_PARAMETERS),
            )
        }
    }
}

/** Returns true if the selector has enough information to identify an item. */
fun ItemSelectorSpec.isComplete(): Boolean {
    return (label != null) ||
        (hotseatRank != null) ||
        (screenIndex != null && x != null && y != null) ||
        (packageName != null && className != null)
}

/** Matches a [WorkspaceItemSpec] at a specific screen index. */
fun ItemSelectorSpec.matchesItemSpec(item: WorkspaceItemSpec, screenIndex: Int): Boolean {
    return matchesItemSpec(
        packageName = item.packageName,
        className = item.className,
        labels = listOf(item.label, item.appLabel, item.title),
        screenIndex = screenIndex,
        x = item.x,
        y = item.y,
        isInDesktop = true,
    ) || item.items?.any { matchesItemSpec(it) } == true
}

/** Matches a [HotseatItemSpec] at a specific hotseat rank. */
fun ItemSelectorSpec.matchesItemSpec(item: HotseatItemSpec, hotseatRank: Int): Boolean {
    return matchesItemSpec(
        packageName = item.packageName,
        className = item.className,
        labels = listOf(item.label, item.appLabel, item.title),
        hotseatRank = hotseatRank,
        isInHotseat = true,
    ) || item.items?.any { matchesItemSpec(it) } == true
}

/** Matches an [AppInFolderSpec] */
fun ItemSelectorSpec.matchesItemSpec(item: AppInFolderSpec): Boolean {
    return matchesItemSpec(
        packageName = item.packageName,
        className = item.className,
        labels = listOf(item.label),
    )
}

/**
 * Base matching logic shared across all item types.
 *
 * This function handles the actual property comparison logic of the [ItemSelectorSpec].
 */
fun ItemSelectorSpec.matchesItemSpec(
    packageName: String? = null,
    className: String? = null,
    labels: List<CharSequence?> = emptyList(),
    screenIndex: Int? = null,
    x: Int? = null,
    y: Int? = null,
    hotseatRank: Int? = null,
    isInHotseat: Boolean = false,
    isInDesktop: Boolean = false,
): Boolean {
    return when {
        this.hotseatRank != null -> {
            isInHotseat && this.hotseatRank == hotseatRank
        }
        this.screenIndex != null && this.x != null && this.y != null -> {
            isInDesktop && this.screenIndex == screenIndex && this.x == x && this.y == y
        }
        this.label != null -> {
            labels.any { it?.toString()?.equals(this.label, ignoreCase = true) == true }
        }
        this.packageName != null && this.className != null -> {
            this.packageName == packageName && this.className == className
        }
        else -> false
    }
}
