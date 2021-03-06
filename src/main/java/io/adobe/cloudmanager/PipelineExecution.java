package io.adobe.cloudmanager;

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

public interface PipelineExecution {

  /**
   * The id of this execution
   *
   * @return the id
   */
  String getId();

  /**
   * The id of the associated program context
   *
   * @return the program id
   */
  String getProgramId();

  /**
   * The id of the associated pipeline context
   *
   * @return the pipeline id
   */
  String getPipelineId();

  /**
   * The current status of the execution
   *
   * @return the status
   */
  Status getStatusState();

  /**
   * Advances this execution if in a valid state.
   *
   * @throws CloudManagerApiException when any error occurs.
   */
  void advance() throws CloudManagerApiException;

  /**
   * Cancel this execution, if in a valid state.
   *
   * @throws CloudManagerApiException when any error occurs.
   */
  void cancel() throws CloudManagerApiException;

  /**
   * Get the url to advance this pipeline.
   *
   * @return the URL to the Advance API endpoint
   * @throws CloudManagerApiException when any error occurs
   */
  String getAdvanceLink() throws CloudManagerApiException;

  /**
   * Get the url to cancel this pipeline.
   *
   * @return the URL to the Cancel API endpoint
   * @throws CloudManagerApiException when any error occurs
   */
  String getCancelLink() throws CloudManagerApiException;

  /**
   * Pipeline Execution status values
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#!AdobeDocs/cloudmanager-api-docs/master/swagger-specs/api.yaml">Cloud Manager Pipeline Model</a>
   */
  enum Status {
    NOT_STARTED("NOT_STARTED"),
    RUNNING("RUNNING"),
    CANCELLING("CANCELLING"),
    CANCELLED("CANCELLED"),
    FINISHED("FINISHED"),
    ERROR("ERROR"),
    FAILED("FAILED");

    private String value;

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

    public String getValue() {
      return value;
    }
  }
}
