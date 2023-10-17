package io.adobe.cloudmanager;

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

import java.io.File;
import java.util.function.Predicate;

import lombok.Getter;

public interface PipelineExecutionStepState {

  /**
   * The id of this execution step state.
   *
   * @return the id
   */
  String getId();

  /**
   * The id of the step in the pipeline.
   *
   * @return the step id
   */
  String getStepId();

  /**
   * The id of the phase in the pipeline.
   *
   * @return the phase id
   */
  String getPhaseId();

  /**
   * The name of the step action, see {@link StepAction}
   *
   * @return the action
   */
  String getAction();

  /**
   * The current status of the pipeline execution step
   *
   * @return the status
   */
  Status getStatusState();

  /**
   * Return the execution associated with this step state.
   *
   * @return pipeline execution
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecution getExecution() throws CloudManagerApiException;

  /**
   * Indicates if this step has a log which can be downloaded.
   *
   * @return {@code true} if a log exists, {@code false} otherwise.
   */
  boolean hasLogs();

  /**
   * Saves the default log to the specified directory.
   *
   * @param dir the directory in which to save the file
   * @throws CloudManagerApiException when any error occurs
   */
  void getLog(File dir) throws CloudManagerApiException;

  /**
   * Streams the specified log to the specified directory.
   *
   * @param name The name of the log to retrieve
   * @param dir  the directory in which to save the file
   * @throws CloudManagerApiException when any error occurs
   */
  void getLog(String name, File dir) throws CloudManagerApiException;

  /**
   * Pipeline Execution Step status values
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#!AdobeDocs/cloudmanager-api-docs/master/swagger-specs/api.yaml">Cloud Manager Pipeline Model</a>
   */
  @Getter
  enum Status {
    NOT_STARTED("NOT_STARTED"),
    RUNNING("RUNNING"),
    FINISHED("FINISHED"),
    ERROR("ERROR"),
    ROLLING_BACK("ROLLING_BACK"),
    ROLLED_BACK("ROLLED_BACK"),
    WAITING("WAITING"),
    CANCELLED("CANCELLED"),
    FAILED("FAILED");

    private final String value;

    Status(String value) {
      this.value = value;
    }

    public static Status fromValue(String text) {
      for (Status b : Status.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  /**
   * Predicate for pipelines for the current step in the execution.
   */
  Predicate<PipelineExecutionStepState> IS_CURRENT = (stepState ->
      stepState.getStatusState() != PipelineExecutionStepState.Status.FINISHED);

  /**
   * Predicate for the step that is waiting.
   */
  Predicate<PipelineExecutionStepState> IS_WAITING = (stepState ->
      stepState.getStatusState() == PipelineExecutionStepState.Status.WAITING
  );

  /**
   * Predicate for the step that is in the running state.
   */
  Predicate<PipelineExecutionStepState> IS_RUNNING = (stepState ->
      stepState.getStatusState() == PipelineExecutionStepState.Status.RUNNING
  );
}
