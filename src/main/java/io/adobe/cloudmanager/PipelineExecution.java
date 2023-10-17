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

import java.util.Optional;
import java.util.function.Predicate;
import javax.validation.constraints.NotNull;

import lombok.Getter;

public interface PipelineExecution {

  /**
   * The id of this execution.
   *
   * @return the id
   */
  @NotNull
  String getId();

  /**
   * The id of the associated program context.
   *
   * @return the program id
   */
  @NotNull
  String getProgramId();

  /**
   * The id of the associated pipeline context.
   *
   * @return the pipeline id
   */
  @NotNull
  String getPipelineId();

  /**
   * The current status of the execution.
   *
   * @return the status
   */
  @NotNull
  Status getStatusState();

  /**
   * Returns the specified action step for this pipeline execution.
   * <p>
   * Note: This does not check the <i>current</i> remote state. It only checks the state of this object. To check current state, retrieve a new PipelineExecution instance.
   *
   * @param action the step state action (see {@link StepAction})
   * @return the step state details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getStep(@NotNull StepAction action) throws CloudManagerApiException;

  /**
   * Gets the current/active step for the execution.
   * <p>
   * Note: This does not check the <i>current</i> remote state. It only checks the state of this object. To check current state, retrieve a new PipelineExecution instance.
   *
   * @return the current step
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getCurrentStep() throws CloudManagerApiException;

  /**
   * Finds the first step in the execution that matches the predicate.
   *
   * @param predicate the filter criteria
   * @return the step state if it exists
   */
  @NotNull
  Optional<PipelineExecutionStepState> getStep(@NotNull Predicate<PipelineExecutionStepState> predicate);

  /**
   * Advances this execution if in a valid state.
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void advance() throws CloudManagerApiException;

  /**
   * Cancel this execution, if in a valid state.
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void cancel() throws CloudManagerApiException;

  /**
   * Checks if this execution is currently running.
   * <p>
   * Note: This does not check the <i>current</i> remote state. It only checks the state of this object. To check current state, retrieve a new PipelineExecution instance.
   *
   * @return true if this execution is running, false otherwise
   */
  boolean isRunning();

  /**
   * Pipeline Execution status values
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#!AdobeDocs/cloudmanager-api-docs/master/swagger-specs/api.yaml">Cloud Manager Pipeline Model</a>
   */
  @Getter
  enum Status {
    NOT_STARTED("NOT_STARTED"),
    RUNNING("RUNNING"),
    CANCELLING("CANCELLING"),
    CANCELLED("CANCELLED"),
    FINISHED("FINISHED"),
    ERROR("ERROR"),
    FAILED("FAILED");

    private final String value;

    Status(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static Status fromValue(String text) {
      for (Status b : Status.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }
}
