package io.adobe.cloudmanager.util;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 Adobe Inc.
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

import io.adobe.cloudmanager.model.Pipeline;

import java.util.function.Predicate;

/**
 * Predicates used to filter response lists from the API calls.
 */
public class Predicates {

  /**
   * Filters pipelines based on BUSY status.
   */
  public static final Predicate<Pipeline> IS_BUSY = (pipeline ->
      io.adobe.cloudmanager.swagger.model.Pipeline.StatusEnum.BUSY == pipeline.getStatus()
  );
}
