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

import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;

/**
 * Root interface that represents all Pipeline Execution events that may be published.
 *
 * Consumers should test against an explicit type to determine which event was received.
 */
public interface PipelineExecutionEvent {

  /**
   * The id of this event.
   *
   * @return the id
   */
  @NotNull
  String getId();

  /**
   * The time at which the event was published.
   *
   * @return the event published time
   */
  @NotNull
  OffsetDateTime getPublished();
}
