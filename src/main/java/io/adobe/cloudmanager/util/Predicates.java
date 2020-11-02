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

import java.util.function.Predicate;

import io.adobe.cloudmanager.model.Pipeline;
import io.adobe.cloudmanager.swagger.model.Metric;
import io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState;
import static io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState.*;


/**
 * Predicates used to filter response lists from the API calls.
 */
public class Predicates {

  /**
   * Predicate for pipelines based on BUSY status.
   */
  public static final Predicate<Pipeline> IS_BUSY = (pipeline ->
      io.adobe.cloudmanager.swagger.model.Pipeline.StatusEnum.BUSY == pipeline.getStatus()
  );

  /**
   * Predicate for pipelines based on they are the current execution.
   */
  public static final Predicate<PipelineExecutionStepState> IS_CURRENT = (stepState ->
    stepState.getStatus() != StatusEnum.FINISHED
  );

  /**
   * Predicate for pipelines that are in a waiting state.
   */
  public static final Predicate<PipelineExecutionStepState> IS_WAITING = (stepState ->
    stepState.getStatus() == StatusEnum.WAITING
  );

  /**
   * Filters metrics that have passed.
   */
  public static final Predicate<Metric> PASSED = Metric::isPassed;

  /**
   * Filters metrics that have failed.
   */
  public static final Predicate<Metric> FAILED = (m -> !m.isPassed());

  /**
   * Filters metrics that are of critical importance.
    */
  public static final Predicate<Metric> CRITICAL = (m -> Metric.SeverityEnum.CRITICAL.equals(m.getSeverity()));

  /**
   * Filters metrics that are important.
   */
  public static final Predicate<Metric> IMPORTANT = (m -> Metric.SeverityEnum.IMPORTANT.equals(m.getSeverity()));
}
