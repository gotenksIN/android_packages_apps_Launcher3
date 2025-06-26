/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.taskbar;

import com.android.launcher3.model.data.ItemInfo;
import dagger.Module;
import dagger.Provides;
import java.util.Collections;
import java.util.Set;
import javax.inject.Named;

/**
 * Dagger module for providing dependencies required for Taskbar direct boot.
 */
@Module
public class TaskbarBootModule {
    @Provides
    @Named("MODEL_ITEMS")
    public Set<ItemInfo> provideModelItems() {
        // In direct boot, there are no extra model items to consider for migration.
        return Collections.emptySet();
    }
}
