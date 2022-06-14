package com.adobe.aio.cloudmanager;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2021 Adobe Inc.
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

import java.io.OutputStream;


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
   * The name of the step action.
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
   * Indicates whether or not this step has a log which can be downloaded.
   *
   * @return {@code true} if a log exists, {@code false} otherwise.
   */
  boolean hasLogs();

  /**
   * Streams the default log, if any, for this step to the specified output stream.
   *
   * @param outputStream the output stream to write to
   * @throws CloudManagerApiException when any error occurs
   */
  void getLog(OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified log, if any, for this step to the specified output stream.
   *
   * @param name         The name of the log to retrieve
   * @param outputStream the output stream to write to
   * @throws CloudManagerApiException when any error occurs
   */
  void getLog(String name, OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Pipeline Execution Step status values
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#!AdobeDocs/cloudmanager-api-docs/master/swagger-specs/api.yaml">Cloud Manager Pipeline Model</a>
   */
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

    private String value;

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

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

}
