/*
 * Copyright 2022 Ververica Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronos.cdc.source.base.config;

import com.kronos.cdc.source.base.options.StartupOptions;
import java.io.Serializable;

/** The source configuration which offers basic source configuration. */
public interface SourceConfig extends Serializable {

    StartupOptions getStartupOptions();

    int getSplitSize();

    int getSplitMetaGroupSize();

    double getDistributionFactorUpper();

    double getDistributionFactorLower();

    boolean isIncludeSchemaChanges();

    /** Factory for the {@code SourceConfig}. */
    @FunctionalInterface
    interface Factory extends Serializable {

        SourceConfig create(int subtask);
    }
}
