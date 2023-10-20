package com.adobe.aio.cloudmanager;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
 * %%
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
 * #L%
 */

import javax.validation.constraints.NotNull;

/**
 * Event representing that a Pipeline Execution Step has started.
 */
public interface PipelineExecutionStepStartEvent extends PipelineExecutionEvent {

  /**
   * Get the Pipeline Execution Step associated with the event.
   *
   * @return the pipeline execution step
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getStepState() throws CloudManagerApiException;
}
